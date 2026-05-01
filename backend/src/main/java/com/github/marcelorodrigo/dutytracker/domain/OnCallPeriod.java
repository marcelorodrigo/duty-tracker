package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDateTime;

public record OnCallPeriod(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime createdAt) {}
