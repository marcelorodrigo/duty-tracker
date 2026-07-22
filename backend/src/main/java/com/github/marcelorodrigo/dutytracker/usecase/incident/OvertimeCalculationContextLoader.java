package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OvertimeCalculationContextLoader {

    private final EngineerProfileGateway engineerProfileGateway;
    private final HolidayGateway holidayGateway;
    private final CompensationRateGateway compensationRateGateway;

    public OvertimeCalculationContext load(Long periodId) {
        var profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));
        var holidays = holidayGateway.findByOnCallPeriodId(periodId);
        var ratesByDayType =
                groupByDayType(compensationRateGateway.findByRateCategory(RateCategory.OVERTIME_ALLOWANCE));

        return new OvertimeCalculationContext(profile, holidays, ratesByDayType);
    }

    private static Map<OvertimeDayType, List<CompensationRate>> groupByDayType(List<CompensationRate> rates) {
        return rates.stream()
                .collect(Collectors.groupingBy(
                        CompensationRate::overtimeDayType,
                        () -> new EnumMap<>(OvertimeDayType.class),
                        Collectors.toUnmodifiableList()));
    }
}
