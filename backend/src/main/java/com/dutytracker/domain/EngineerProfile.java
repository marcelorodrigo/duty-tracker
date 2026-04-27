package com.dutytracker.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;

public record EngineerProfile(
        Long id,
        EmployeeType employeeType,
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Instant createdAt) {}
