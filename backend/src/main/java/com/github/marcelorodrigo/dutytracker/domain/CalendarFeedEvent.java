package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDateTime;

public record CalendarFeedEvent(String summary, LocalDateTime startDateTime, LocalDateTime endDateTime) {}
