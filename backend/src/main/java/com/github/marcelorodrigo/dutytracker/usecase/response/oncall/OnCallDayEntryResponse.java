package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OnCallDayEntryResponse(LocalDate date, BigDecimal hours, StandbyRateType rateType, boolean capped) {}
