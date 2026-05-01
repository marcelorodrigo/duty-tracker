package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import java.time.LocalDate;

public record RemoveHolidayOverrideRequest(Long periodId, LocalDate date) {}
