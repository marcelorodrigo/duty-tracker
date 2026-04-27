package com.dutytracker.application.usecase.oncall;

import java.time.LocalDate;

public record AddHolidayOverrideRequest(Long periodId, LocalDate date) {
}
