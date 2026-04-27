package com.dutytracker.usecase.request.oncall;

import java.time.LocalDate;

public record RemoveHolidayOverrideRequest(Long periodId, LocalDate date) {}
