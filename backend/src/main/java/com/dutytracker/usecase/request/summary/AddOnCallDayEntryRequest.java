package com.dutytracker.usecase.request.summary;

import com.dutytracker.domain.StandbyRateType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AddOnCallDayEntryRequest(
        Long onCallPeriodId, LocalDate date, BigDecimal hours, StandbyRateType rateType) {}
