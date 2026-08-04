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
        BigDecimal standbyWeekdaySundayHolidayPercentage,
        String calendarFeedUrl) {

    public EngineerProfileResponse(
            Long id,
            List<String> workingDays,
            LocalTime workStartTime,
            LocalTime workEndTime,
            BigDecimal hourlyRate,
            BigDecimal standbyWeekdaySaturdayPercentage,
            BigDecimal standbyWeekdaySundayHolidayPercentage) {
        this(
                id,
                workingDays,
                workStartTime,
                workEndTime,
                hourlyRate,
                standbyWeekdaySaturdayPercentage,
                standbyWeekdaySundayHolidayPercentage,
                null);
    }
}
