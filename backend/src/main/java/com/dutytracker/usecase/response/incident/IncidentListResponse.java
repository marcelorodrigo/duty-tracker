package com.dutytracker.usecase.response.incident;

import java.util.List;

public record IncidentListResponse(List<IncidentResponse> incidents) {
}
