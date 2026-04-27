package com.dutytracker.application.usecase.compensation;

import com.dutytracker.domain.model.EmployeeType;

public record GetCompensationRateTableRequest(EmployeeType employeeType) {}
