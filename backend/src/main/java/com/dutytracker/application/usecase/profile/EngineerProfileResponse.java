package com.dutytracker.application.usecase.profile;

import com.dutytracker.domain.model.EmployeeType;

import java.time.LocalTime;
import java.util.List;

public record EngineerProfileResponse(
        Long id,
        EmployeeType employeeType,
        List<String> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        boolean locked
) {}
