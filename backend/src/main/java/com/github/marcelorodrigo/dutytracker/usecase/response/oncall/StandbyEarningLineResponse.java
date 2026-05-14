package com.github.marcelorodrigo.dutytracker.usecase.response.oncall;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StandbyEarningLineResponse(
        LocalDate date,
        String dayLabel,
        String compensationLabel,
        BigDecimal hours,
        BigDecimal amount,
        boolean capped) {}
