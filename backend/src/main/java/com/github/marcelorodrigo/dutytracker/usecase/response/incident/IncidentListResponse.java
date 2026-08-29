package com.github.marcelorodrigo.dutytracker.usecase.response.incident;

import java.util.List;

public record IncidentListResponse(
        List<IncidentResponse> content, int page, int size, long totalElements, int totalPages) {}
