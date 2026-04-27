package com.dutytracker.application.usecase.profile;

import com.dutytracker.domain.model.EmployeeType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record CreateEngineerProfileRequest(
        EmployeeType employeeType,
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime
) {}
