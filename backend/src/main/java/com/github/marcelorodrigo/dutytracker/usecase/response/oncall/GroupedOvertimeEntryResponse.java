package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GroupedOvertimeEntryResponse(
        LocalDate date,
        boolean isAllowanceEntry,
        BigDecimal allowancePercentage,
        BigDecimal hours,
        List<Long> incidentIds) {}
