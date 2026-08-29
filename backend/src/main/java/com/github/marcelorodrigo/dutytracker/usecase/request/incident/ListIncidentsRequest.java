package com.github.marcelorodrigo.dutytracker.usecase.request.incident;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;

public record ListIncidentsRequest(Long onCallPeriodId, PaginationRequest pagination) {}
