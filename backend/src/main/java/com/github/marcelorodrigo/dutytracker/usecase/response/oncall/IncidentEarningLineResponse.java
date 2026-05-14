package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;

public record IncidentEarningLineResponse(
        Long incidentId, String incidentName, String hoursSummary, BigDecimal subtotal) {}
