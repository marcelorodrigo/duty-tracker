package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.util.List;

public record OnCallPeriodListResponse(
        List<OnCallPeriodResponse> content, int page, int size, long totalElements, int totalPages) {}
