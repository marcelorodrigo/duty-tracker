package com.github.marcelorodrigo.dutytracker.usecase.request.profile;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record UpdateEngineerProfileRequest(
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        BigDecimal hourlyRate,
        BigDecimal standbyWeekdaySaturdayPercentage,
        BigDecimal standbyWeekdaySundayHolidayPercentage) {}
