package com.dutytracker.usecase.request.oncall;

import java.time.LocalDateTime;

public record UpdateOnCallPeriodRequest(Long periodId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
}
