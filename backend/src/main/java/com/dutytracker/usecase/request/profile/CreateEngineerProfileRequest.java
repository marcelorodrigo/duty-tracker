package com.dutytracker.usecase.request.profile;



import com.dutytracker.domain.EmployeeType;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
public record CreateEngineerProfileRequest(
        EmployeeType employeeType,
        Set<DayOfWeek> workingDays,
        LocalTime workStartTime,
        LocalTime workEndTime
) {}
