package com.dutytracker.application.usecase.oncall;

import java.time.LocalDateTime;

public record UpdateOnCallPeriodRequest(Long periodId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
}
