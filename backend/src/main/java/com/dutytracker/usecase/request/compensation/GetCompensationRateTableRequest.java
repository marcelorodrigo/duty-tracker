package com.dutytracker.usecase.request.compensation;

import com.dutytracker.domain.model.EmployeeType;

public record GetCompensationRateTableRequest(EmployeeType employeeType) {}
