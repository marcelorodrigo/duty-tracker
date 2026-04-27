package com.dutytracker.application.usecase.summary;

import java.math.BigDecimal;

public record OverrideOvertimeEntryRequest(
        Long entryId,
        BigDecimal overtimeHours,
        BigDecimal allowanceHours,
        BigDecimal allowancePercentage
) {}
