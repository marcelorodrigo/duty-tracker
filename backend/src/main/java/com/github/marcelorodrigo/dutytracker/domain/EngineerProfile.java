package com.github.marcelorodrigo.dutytracker.domain;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
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
        LocalDateTime createdAt) {

    public static final int STANDARD_MONTHLY_HOURS = 160;
    private static final BigDecimal MINIMUM_STANDBY_PERCENTAGE = new BigDecimal("0.001");

    public EngineerProfile {
        if (workingDays == null || workingDays.isEmpty()) {
            throw new InvalidEngineerProfileException("At least one working day must be specified");
        }
        workingDays = Set.copyOf(workingDays);
        if (workStartTime == null || workEndTime == null || !workEndTime.isAfter(workStartTime)) {
            throw new InvalidEngineerProfileException("workEndTime must be after workStartTime");
        }
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ONE) < 0) {
            throw new InvalidHourlyRateException("Hourly rate must be at least 1");
        }
        validateStandbyPercentage(standbyWeekdaySaturdayPercentage, "standbyWeekdaySaturdayPercentage");
        validateStandbyPercentage(standbyWeekdaySundayHolidayPercentage, "standbyWeekdaySundayHolidayPercentage");
    }

    public static EngineerProfile create(
            Set<DayOfWeek> workingDays,
            LocalTime workStartTime,
            LocalTime workEndTime,
            BigDecimal hourlyRate,
            BigDecimal standbyWeekdaySaturdayPercentage,
            BigDecimal standbyWeekdaySundayHolidayPercentage) {
        return new EngineerProfile(
                null,
                workingDays,
                workStartTime,
                workEndTime,
                hourlyRate,
                standbyWeekdaySaturdayPercentage,
                standbyWeekdaySundayHolidayPercentage,
                null);
    }

    public EngineerProfile withSettings(
            Set<DayOfWeek> workingDays,
            LocalTime workStartTime,
            LocalTime workEndTime,
            BigDecimal hourlyRate,
            BigDecimal standbyWeekdaySaturdayPercentage,
            BigDecimal standbyWeekdaySundayHolidayPercentage) {
        return new EngineerProfile(
                id,
                workingDays,
                workStartTime,
                workEndTime,
                hourlyRate,
                standbyWeekdaySaturdayPercentage,
                standbyWeekdaySundayHolidayPercentage,
                createdAt);
    }

    private static void validateStandbyPercentage(BigDecimal percentage, String fieldName) {
        if (percentage == null || percentage.compareTo(MINIMUM_STANDBY_PERCENTAGE) < 0) {
            throw new InvalidStandbyPercentageException(fieldName + " must be at least 0.001");
        }
    }
}
