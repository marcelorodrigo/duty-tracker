package com.github.marcelorodrigo.dutytracker.usecase.response.incident;

import java.util.List;
import java.util.Objects;

public record OvertimeEntriesResponse(
        Long incidentId, OvertimeCalculationStatus status, List<OvertimeEntryResponse> entries) {

    public OvertimeEntriesResponse {
        Objects.requireNonNull(incidentId, "incidentId is required");
        Objects.requireNonNull(status, "status is required");
        entries = List.copyOf(entries);

        if (status == OvertimeCalculationStatus.NO_OVERTIME && !entries.isEmpty()) {
            throw new IllegalArgumentException("NO_OVERTIME results cannot contain entries");
        }
        if (status == OvertimeCalculationStatus.OVERTIME_CALCULATED && entries.isEmpty()) {
            throw new IllegalArgumentException("OVERTIME_CALCULATED results require entries");
        }
    }

    public OvertimeEntriesResponse(Long incidentId, List<OvertimeEntryResponse> entries) {
        this(
                incidentId,
                entries.isEmpty()
                        ? OvertimeCalculationStatus.NO_OVERTIME
                        : OvertimeCalculationStatus.OVERTIME_CALCULATED,
                entries);
    }

    public static OvertimeEntriesResponse noOvertime(Long incidentId) {
        return new OvertimeEntriesResponse(incidentId, OvertimeCalculationStatus.NO_OVERTIME, List.of());
    }

    public static OvertimeEntriesResponse calculated(Long incidentId, List<OvertimeEntryResponse> entries) {
        return new OvertimeEntriesResponse(incidentId, OvertimeCalculationStatus.OVERTIME_CALCULATED, entries);
    }
}
