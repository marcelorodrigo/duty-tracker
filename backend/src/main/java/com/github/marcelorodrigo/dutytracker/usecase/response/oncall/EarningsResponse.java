package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EarningsResponse(
        Long periodId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        List<StandbyEarningLineResponse> standbyLines,
        List<IncidentEarningLineResponse> incidentLines,
        BigDecimal grandTotal) {}
