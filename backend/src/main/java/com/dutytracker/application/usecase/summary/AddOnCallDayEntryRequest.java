package com.dutytracker.application.usecase.summary;

import com.dutytracker.domain.model.StandbyRateType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddOnCallDayEntryRequest(
        Long onCallPeriodId,
        LocalDate date,
        BigDecimal hours,
        StandbyRateType rateType
) {}
