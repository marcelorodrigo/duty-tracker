package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.time.LocalDateTime;
import java.util.List;

public record OnCallPeriodResponse(
        Long id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        List<HolidayResponse> holidays,
        LocalDateTime createdAt) {}
