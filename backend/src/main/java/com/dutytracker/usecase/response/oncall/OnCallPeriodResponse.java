package com.dutytracker.usecase.response.oncall;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OnCallPeriodResponse(
        Long id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        List<LocalDate> holidayOverrides,
        LocalDateTime createdAt) {}
