package com.dutytracker.usecase.response.incident;


import java.util.List;
public record OvertimeEntriesResponse(Long incidentId, List<OvertimeEntryResponse> entries) {}
