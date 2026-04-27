package com.dutytracker.domain;

import java.math.BigDecimal;
import com.dutytracker.domain.StandbyRateType;
import java.time.LocalDate;
import com.dutytracker.domain.StandbyRateType;

public record OnCallDayEntry(
        Long id,
        Long onCallPeriodId,
        LocalDate date,
        BigDecimal hours,
        StandbyRateType rateType,
        boolean capped,
        boolean timeForTimeFlag,
        boolean manualOverride
) {
}
