package com.dutytracker.application.usecase.oncall;

import java.time.LocalDateTime;

public record CreateOnCallPeriodRequest(LocalDateTime startDateTime, LocalDateTime endDateTime) {
}
