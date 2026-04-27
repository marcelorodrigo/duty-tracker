package com.dutytracker.usecase.request.oncall;

import java.time.LocalDateTime;

public record CreateOnCallPeriodRequest(LocalDateTime startDateTime, LocalDateTime endDateTime) {}
