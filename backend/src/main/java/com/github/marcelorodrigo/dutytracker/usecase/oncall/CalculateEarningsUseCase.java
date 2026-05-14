package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.incident.CalculateOvertimeEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.EarningsResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.IncidentEarningLineResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.StandbyEarningLineResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateEarningsValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateEarningsUseCase implements UseCase<CalculateEarningsRequest, EarningsResponse> {

    private final CalculateOnCallDayEntriesUseCase calculateOnCallDayEntries;
    private final CalculateOvertimeEntriesUseCase calculateOvertimeEntries;
    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CompensationRateGateway compensationRateGateway;
    private final CalculateEarningsValidator validator;

    @Override
    public EarningsResponse execute(CalculateEarningsRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        EngineerProfile profile = engineerProfileGateway.find().orElseThrow(ProfileNotFoundException::new);

        BigDecimal overtimeBasePercentage =
                findFirstByCategory(RateCategory.OVERTIME_BASE).percentage();

        List<OnCallDayEntryResponse> dayEntries = calculateOnCallDayEntries
                .execute(new CalculateOnCallDayEntriesRequest(periodId))
                .entries();

        List<StandbyEarningLineResponse> standbyLines = new ArrayList<>();
        BigDecimal standbyTotal = BigDecimal.ZERO;

        for (OnCallDayEntryResponse entry : dayEntries) {
            BigDecimal percentage = entry.rateType() == StandbyRateType.WEEKDAY_SATURDAY
                    ? profile.standbyWeekdaySaturdayPercentage()
                    : profile.standbyWeekdaySundayHolidayPercentage();
            String compensationLabel = entry.rateType() == StandbyRateType.WEEKDAY_SATURDAY
                    ? "On-call Monday\u2013Saturday"
                    : "On-call Sunday / Holiday";
            BigDecimal amount = entry.hours()
                    .multiply(profile.hourlyRate())
                    .multiply(percentage)
                    .setScale(2, RoundingMode.HALF_UP);
            standbyLines.add(new StandbyEarningLineResponse(
                    entry.date(), entry.dayLabel(), compensationLabel, entry.hours(), amount, entry.capped()));
            standbyTotal = standbyTotal.add(amount);
        }

        List<Incident> incidents = incidentGateway.findByOnCallPeriodId(periodId);
        List<IncidentEarningLineResponse> incidentLines = new ArrayList<>();
        BigDecimal incidentTotal = BigDecimal.ZERO;

        for (Incident incident : incidents) {
            try {
                OvertimeEntriesResponse overtimeEntries =
                        calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(incident.id()));

                BigDecimal subtotal = calculateIncidentSubtotal(
                        overtimeEntries.entries(), profile.hourlyRate(), overtimeBasePercentage);
                String hoursSummary = buildHoursSummary(overtimeEntries.entries());

                incidentLines.add(
                        new IncidentEarningLineResponse(incident.id(), incident.name(), hoursSummary, subtotal));
                incidentTotal = incidentTotal.add(subtotal);
            } catch (IncidentDuringWorkingHoursException e) {
                // Incident falls entirely within working hours — no earnings to report
            }
        }

        BigDecimal grandTotal = standbyTotal.add(incidentTotal).setScale(2, RoundingMode.HALF_UP);

        return new EarningsResponse(
                periodId, period.startDateTime(), period.endDateTime(), standbyLines, incidentLines, grandTotal);
    }

    private com.github.marcelorodrigo.dutytracker.domain.CompensationRate findFirstByCategory(RateCategory category) {
        return compensationRateGateway.findByRateCategory(category).stream()
                .findFirst()
                .orElseThrow(
                        () -> new CompensationRateNotFoundException("No compensation rate found for: " + category));
    }

    private BigDecimal calculateIncidentSubtotal(
            List<OvertimeEntryResponse> entries, BigDecimal hourlyRate, BigDecimal overtimeBasePercentage) {
        BigDecimal total = BigDecimal.ZERO;
        for (OvertimeEntryResponse entry : entries) {
            if (entry.isAllowanceEntry()) {
                BigDecimal amount = entry.allowanceHours()
                        .multiply(hourlyRate)
                        .multiply(entry.allowancePercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                total = total.add(amount);
            } else {
                BigDecimal amount = entry.overtimeHours()
                        .multiply(hourlyRate)
                        .multiply(overtimeBasePercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                total = total.add(amount);
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Builds a human-readable hours summary string for an incident.
     * Example: "3h overtime + 2h 50% allowance + 1h 35% allowance"
     * Hours values from CalculateOvertimeEntriesUseCase are ceiled to whole hours.
     */
    private String buildHoursSummary(List<OvertimeEntryResponse> entries) {
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        // Use TreeMap with reversed order so highest percentage comes first
        TreeMap<BigDecimal, BigDecimal> allowanceByPct = new TreeMap<>(Comparator.reverseOrder());

        for (OvertimeEntryResponse entry : entries) {
            if (entry.isAllowanceEntry()) {
                allowanceByPct.merge(entry.allowancePercentage(), entry.allowanceHours(), BigDecimal::add);
            } else {
                totalOvertimeHours = totalOvertimeHours.add(entry.overtimeHours());
            }
        }

        List<String> parts = new ArrayList<>();

        if (totalOvertimeHours.compareTo(BigDecimal.ZERO) > 0) {
            parts.add(formatHours(totalOvertimeHours) + "h overtime");
        }

        for (Map.Entry<BigDecimal, BigDecimal> entry : allowanceByPct.entrySet()) {
            parts.add(formatHours(entry.getValue()) + "h " + formatPct(entry.getKey()) + "% allowance");
        }

        return String.join(" + ", parts);
    }

    private String formatHours(BigDecimal hours) {
        return hours.stripTrailingZeros().toPlainString();
    }

    private String formatPct(BigDecimal pct) {
        return pct.stripTrailingZeros().toPlainString();
    }
}
