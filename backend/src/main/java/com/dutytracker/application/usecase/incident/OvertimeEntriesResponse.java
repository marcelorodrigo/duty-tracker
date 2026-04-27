package com.dutytracker.application.usecase.incident;

import java.util.List;

public record OvertimeEntriesResponse(Long incidentId, List<OvertimeEntryResponse> entries) {}
