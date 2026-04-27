package com.dutytracker.domain.model;

import java.time.LocalDate;

public record HolidayOverride(
        Long id,
        Long onCallPeriodId,
        LocalDate date
) {
}
