package com.github.marcelorodrigo.dutytracker.domain;

import java.time.LocalDate;

public record OnCallDayEntry(
        Long onCallPeriodId, LocalDate date, Hours hours, StandbyRateType rateType, boolean capped) {}
