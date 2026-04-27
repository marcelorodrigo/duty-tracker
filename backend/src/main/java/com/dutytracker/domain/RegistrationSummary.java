package com.dutytracker.domain;


import java.time.Instant;
import java.time.LocalDate;
public record RegistrationSummary(
        Long id,
        String label,
        LocalDate periodStart,
        LocalDate periodEnd,
        Instant createdAt,
        Instant updatedAt
) {
}
