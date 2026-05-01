package com.github.marcelorodrigo.dutytracker.usecase.response.profile;

import java.time.LocalTime;
import java.util.List;

public record EngineerProfileResponse(
        Long id, List<String> workingDays, LocalTime workStartTime, LocalTime workEndTime) {}
