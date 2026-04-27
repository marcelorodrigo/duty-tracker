package com.dutytracker.usecase.response.oncall;

import com.dutytracker.domain.StandbyRateType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OnCallDayEntryResponse(
        Long id,
        LocalDate date,
        BigDecimal hours,
        StandbyRateType rateType,
        boolean capped,
        boolean timeForTimeFlag,
        boolean manualOverride) {}
