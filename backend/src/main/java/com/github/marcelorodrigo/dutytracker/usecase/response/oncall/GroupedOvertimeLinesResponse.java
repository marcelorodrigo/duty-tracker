package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.util.List;

public record GroupedOvertimeLinesResponse(List<GroupedOvertimeEntryResponse> entries) {}
