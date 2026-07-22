package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDate;

public record OnCallDayEntry(
        Long onCallPeriodId, LocalDate date, int minutes, StandbyRateType rateType, boolean capped) {}
