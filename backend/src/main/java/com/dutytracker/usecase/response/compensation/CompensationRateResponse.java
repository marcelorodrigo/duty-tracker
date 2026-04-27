package com.dutytracker.usecase.response.compensation;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.domain.RateCategory;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CompensationRateResponse(
        Long id,
        EmployeeType employeeType,
        RateCategory rateCategory,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal percentage
) {}
