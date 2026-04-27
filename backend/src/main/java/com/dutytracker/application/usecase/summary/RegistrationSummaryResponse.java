package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.application.usecase.oncall.OnCallDayEntryResponse;

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
