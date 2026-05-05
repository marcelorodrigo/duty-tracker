package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.time.LocalDate;

public record HolidayResponse(LocalDate date, String name) {}
