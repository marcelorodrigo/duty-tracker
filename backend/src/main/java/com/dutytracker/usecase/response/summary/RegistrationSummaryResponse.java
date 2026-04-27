package com.dutytracker.usecase.response.summary;

import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RegistrationSummaryResponse(
        Long id,
        String label,
        LocalDate periodStart,
        LocalDate periodEnd,
        Instant createdAt,
        Instant updatedAt,
        List<OnCallDayEntryResponse> onCallEntries,
        List<OvertimeEntryResponse> overtimeEntries
) {}
