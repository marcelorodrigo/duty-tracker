package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import java.time.LocalDateTime;

public record CreateOnCallPeriodRequest(LocalDateTime startDateTime, LocalDateTime endDateTime) {}
