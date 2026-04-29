package com.dutytracker.usecase.request.profile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record CreateEngineerProfileRequest(
        Set<DayOfWeek> workingDays, LocalTime workStartTime, LocalTime workEndTime) {}
