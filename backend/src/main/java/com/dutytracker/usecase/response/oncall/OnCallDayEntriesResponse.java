package com.dutytracker.usecase.response.oncall;


import java.util.List;
public record OnCallDayEntriesResponse(
        Long periodId,
        List<OnCallDayEntryResponse> entries
) {}
