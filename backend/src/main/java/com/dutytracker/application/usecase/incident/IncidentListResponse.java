package com.dutytracker.application.usecase.incident;

import java.util.List;

public record IncidentListResponse(List<IncidentResponse> incidents) {
}
