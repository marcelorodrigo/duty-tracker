package com.dutytracker.domain;

import java.time.LocalDate;

public record HolidayOverride(Long id, Long onCallPeriodId, LocalDate date) {}
