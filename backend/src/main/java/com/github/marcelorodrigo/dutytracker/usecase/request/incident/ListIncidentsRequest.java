package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import org.springframework.data.domain.Pageable;

public record ListIncidentsRequest(Long onCallPeriodId, Pageable pageable) {}
