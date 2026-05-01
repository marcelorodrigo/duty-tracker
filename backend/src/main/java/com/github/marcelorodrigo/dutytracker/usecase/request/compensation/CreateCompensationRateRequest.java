package com.github.marcelorodrigo.dutytracker.usecase.request.compensation;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import java.math.BigDecimal;
import java.time.LocalTime;

public record CreateCompensationRateRequest(
        OvertimeDayType overtimeDayType, String label, LocalTime timeFrom, LocalTime timeTo, BigDecimal percentage) {}
