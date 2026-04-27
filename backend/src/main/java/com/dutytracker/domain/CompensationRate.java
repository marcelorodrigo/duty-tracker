package com.dutytracker.domain;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CompensationRate(
        Long id,
        EmployeeType employeeType,
        RateCategory rateCategory,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal percentage) {}
