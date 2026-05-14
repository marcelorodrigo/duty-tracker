package com.github.marcelorodrigo.dutytracker.usecase.response.profile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record EngineerProfileResponse(
        Long id,
        List<String> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        BigDecimal hourlyRate,
        BigDecimal standbyWeekdaySaturdayPercentage,
        BigDecimal standbyWeekdaySundayHolidayPercentage) {}
