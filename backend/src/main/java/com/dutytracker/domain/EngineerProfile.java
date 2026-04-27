package com.dutytracker.domain;

import java.time.DayOfWeek;
import com.dutytracker.domain.EmployeeType;
import java.time.Instant;
import com.dutytracker.domain.EmployeeType;
import java.time.LocalTime;
import com.dutytracker.domain.EmployeeType;
import java.util.Set;
import com.dutytracker.domain.EmployeeType;

public record EngineerProfile(
        Long id,
        EmployeeType employeeType,
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Instant createdAt
) {
}
