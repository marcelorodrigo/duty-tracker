package com.dutytracker.usecase.request.compensation;

import com.dutytracker.domain.model.EmployeeType;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CreateCompensationRateRequest(
        EmployeeType employeeType,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal percentage
) {}
