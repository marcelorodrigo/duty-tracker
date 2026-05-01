package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import java.time.LocalDate;

public record AddHolidayOverrideRequest(Long periodId, LocalDate date) {}
