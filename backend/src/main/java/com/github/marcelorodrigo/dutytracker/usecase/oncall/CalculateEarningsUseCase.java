package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Hours;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public EarningsResponse execute(CalculateEarningsRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        EngineerProfile profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));

        Percentage overtimeBasePercentage =
                findFirstByCategory(RateCategory.OVERTIME_BASE).percentage();

        List<OnCallDayEntryResponse> dayEntries = calculateOnCallDayEntries
                .execute(new CalculateOnCallDayEntriesRequest(periodId))
                .entries();

        List<StandbyEarningLineResponse> standbyLines = new ArrayList<>();
        Money standbyTotal = Money.zero();

        for (OnCallDayEntryResponse entry : dayEntries) {
            Percentage percentage = entry.rateType() == StandbyRateType.WEEKDAY_SATURDAY
                    ? profile.standbyWeekdaySaturdayPercentage()
                    : profile.standbyWeekdaySundayHolidayPercentage();
            String compensationLabel = entry.rateType() == StandbyRateType.WEEKDAY_SATURDAY
                    ? "On-call Monday\u2013Saturday"
                    : "On-call Sunday / Holiday";
            Hours hours = new Hours(entry.hours());
            Money amount = profile.hourlyRate()
                    .multiply(hours)
                    .multiply(EngineerProfile.STANDARD_MONTHLY_HOURS)
                    .apply(percentage);
            standbyLines.add(new StandbyEarningLineResponse(
                    entry.date(),
                    entry.dayLabel(),
                    compensationLabel,
                    hours.value(),
                    amount.toApiAmount(),
                    entry.capped()));
            standbyTotal = standbyTotal.add(amount);
        }

        List<Incident> incidents = incidentGateway.findByOnCallPeriodId(periodId);
        List<IncidentEarningLineResponse> incidentLines = new ArrayList<>();
        Money incidentTotal = Money.zero();

        for (Incident incident : incidents) {
            try {
                OvertimeEntriesResponse overtimeEntries =
                        calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(incident.id()));

                Money subtotal = calculateIncidentSubtotal(
                        overtimeEntries.entries(), profile.hourlyRate(), overtimeBasePercentage);
                String hoursSummary = buildHoursSummary(overtimeEntries.entries());

                incidentLines.add(new IncidentEarningLineResponse(
                        incident.id(), incident.name(), hoursSummary, subtotal.toApiAmount()));
                incidentTotal = incidentTotal.add(subtotal);
            } catch (IncidentDuringWorkingHoursException _) {
                // Incident falls entirely within working hours — no earnings to report
            }
        }

        Money grandTotal = standbyTotal.add(incidentTotal);

        return new EarningsResponse(
                periodId,
                period.startDateTime(),
                period.endDateTime(),
                standbyLines,
                incidentLines,
                grandTotal.toApiAmount());
    }

    private CompensationRate findFirstByCategory(RateCategory category) {
        final var rates = compensationRateGateway.findByRateCategory(category);
        if (rates.isEmpty()) {
            throw new CompensationRateNotFoundException("No compensation rate found for: " + category);
        }
        if (rates.size() > 1) {
            throw new IllegalStateException("Ambiguous compensation rate: expected exactly one rate for " + category
                    + " but found " + rates.size());
        }
        return rates.getFirst();
    }

    private Money calculateIncidentSubtotal(
            List<OvertimeEntryResponse> entries, Money hourlyRate, Percentage overtimeBasePercentage) {
        Money total = Money.zero();
        for (OvertimeEntryResponse entry : entries) {
            if (entry.isAllowanceEntry()) {
                Money amount = hourlyRate
                        .multiply(new Hours(entry.allowanceHours()))
                        .apply(Percentage.of(entry.allowancePercentage()));
                total = total.add(amount);
            } else {
                Money amount =
                        hourlyRate.multiply(new Hours(entry.overtimeHours())).apply(overtimeBasePercentage);
                total = total.add(amount);
            }
        }
        return total;
    }

    /**
     * Builds a human-readable hours summary string for an incident.
     * Example: "3h overtime + 2h 50% allowance + 1h 35% allowance"
     * Hours values from CalculateOvertimeEntriesUseCase are ceiled to whole hours.
     */
    private String buildHoursSummary(List<OvertimeEntryResponse> entries) {
        Hours totalOvertimeHours = Hours.zero();
        // Use TreeMap with reversed order so highest percentage comes first
        TreeMap<Percentage, Hours> allowanceByPct = new TreeMap<>(Comparator.reverseOrder());

        for (OvertimeEntryResponse entry : entries) {
            if (entry.isAllowanceEntry()) {
                allowanceByPct.merge(
                        Percentage.of(entry.allowancePercentage()), new Hours(entry.allowanceHours()), Hours::add);
            } else {
                totalOvertimeHours = totalOvertimeHours.add(new Hours(entry.overtimeHours()));
            }
        }

        List<String> parts = new ArrayList<>();

        if (totalOvertimeHours.isPositive()) {
            parts.add(formatHours(totalOvertimeHours) + "h overtime");
        }

        for (Map.Entry<Percentage, Hours> entry : allowanceByPct.entrySet()) {
            parts.add(formatHours(entry.getValue()) + "h " + formatPct(entry.getKey()) + "% allowance");
        }

        return String.join(" + ", parts);
    }

    private String formatHours(Hours hours) {
        return hours.value().stripTrailingZeros().toPlainString();
    }

    private String formatPct(Percentage percentage) {
        return percentage.value().stripTrailingZeros().toPlainString();
    }
}
