package com.dutytracker.application.usecase.oncall;

import java.time.LocalDate;

public record RemoveHolidayOverrideRequest(Long periodId, LocalDate date) {
}
