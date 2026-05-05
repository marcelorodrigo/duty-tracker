package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import java.time.LocalDate;

public record GetHolidaySuggestionsRequest(LocalDate start, LocalDate end) {}
