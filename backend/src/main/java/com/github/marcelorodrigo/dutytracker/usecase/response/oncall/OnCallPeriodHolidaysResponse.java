package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.util.List;

public record OnCallPeriodHolidaysResponse(List<HolidayResponse> holidays) {}
