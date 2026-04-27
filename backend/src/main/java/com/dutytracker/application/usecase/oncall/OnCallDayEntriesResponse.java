package com.dutytracker.application.usecase.oncall;

import java.util.List;

public record OnCallDayEntriesResponse(
        Long periodId,
        List<OnCallDayEntryResponse> entries
) {}
