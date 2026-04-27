package com.dutytracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

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
