package com.dutytracker.application.usecase.oncall;

import com.dutytracker.domain.model.StandbyRateType;

import java.math.BigDecimal;

public record OverrideOnCallDayEntryRequest(
        Long entryId,
        BigDecimal hours,
        StandbyRateType rateType,
        Boolean timeForTimeFlag
) {}
