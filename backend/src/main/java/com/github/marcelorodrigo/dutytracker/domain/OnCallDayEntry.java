package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OnCallDayEntry(
        Long onCallPeriodId, LocalDate date, BigDecimal hours, StandbyRateType rateType, boolean capped) {}
