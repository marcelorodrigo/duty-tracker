package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public record EngineerProfile(
        Long id,
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        BigDecimal hourlyRate,
        BigDecimal standbyWeekdaySaturdayPercentage,
        BigDecimal standbyWeekdaySundayHolidayPercentage,
        String calendarFeedUrl,
        LocalDateTime createdAt) {

    public static final int STANDARD_MONTHLY_HOURS = 160;

    public EngineerProfile(
            Long id,
            Set<DayOfWeek> workingDays,
            LocalTime workStartTime,
            LocalTime workEndTime,
            BigDecimal hourlyRate,
            BigDecimal standbyWeekdaySaturdayPercentage,
            BigDecimal standbyWeekdaySundayHolidayPercentage,
            LocalDateTime createdAt) {
        this(
                id,
                workingDays,
                workStartTime,
                workEndTime,
                hourlyRate,
                standbyWeekdaySaturdayPercentage,
                standbyWeekdaySundayHolidayPercentage,
                null,
                createdAt);
    }
}
