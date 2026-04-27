package com.dutytracker.usecase.response.compensation;

import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.domain.model.RateCategory;

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
