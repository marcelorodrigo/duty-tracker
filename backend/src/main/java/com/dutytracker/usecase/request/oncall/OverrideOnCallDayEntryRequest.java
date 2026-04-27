package com.dutytracker.usecase.request.oncall;



import com.dutytracker.domain.StandbyRateType;
import java.math.BigDecimal;
public record OverrideOnCallDayEntryRequest(
        Long entryId,
        BigDecimal hours,
        StandbyRateType rateType,
        Boolean timeForTimeFlag
) {}
