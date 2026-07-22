package com.github.marcelorodrigo.dutytracker.domain;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import java.time.LocalTime;

public record CompensationRate(
        Long id,
        RateCategory rateCategory,
        OvertimeDayType overtimeDayType,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        Percentage percentage) {

    public CompensationRate {
        if (rateCategory == null) {
            throw new InvalidCompensationRateException("rateCategory is required");
        }
        if (label == null || label.isBlank()) {
            throw new InvalidCompensationRateException("label is required");
        }
        if (percentage == null || percentage.isNegative()) {
            throw new InvalidCompensationRateException("percentage must be >= 0");
        }

        boolean isOvertimeAllowance = rateCategory == RateCategory.OVERTIME_ALLOWANCE;
        if (isOvertimeAllowance && (overtimeDayType == null || timeFrom == null || timeTo == null)) {
            throw new InvalidCompensationRateException(
                    "Overtime allowance rates require overtimeDayType, timeFrom and timeTo");
        }
        if (!isOvertimeAllowance && (overtimeDayType != null || timeFrom != null || timeTo != null)) {
            throw new InvalidCompensationRateException(
                    "Only overtime allowance rates may define overtimeDayType, timeFrom or timeTo");
        }
    }

    public static CompensationRate overtimeAllowance(
            OvertimeDayType overtimeDayType,
            String label,
            LocalTime timeFrom,
            LocalTime timeTo,
            Percentage percentage) {
        return new CompensationRate(
                null, RateCategory.OVERTIME_ALLOWANCE, overtimeDayType, label, timeFrom, timeTo, percentage);
    }

    public CompensationRate withDetails(String label, Percentage percentage) {
        return new CompensationRate(id, rateCategory, overtimeDayType, label, timeFrom, timeTo, percentage);
    }
}
