package com.dutytracker.domain;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CompensationRate(
        Long id,
        RateCategory rateCategory,
        OvertimeDayType overtimeDayType,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal percentage) {}
