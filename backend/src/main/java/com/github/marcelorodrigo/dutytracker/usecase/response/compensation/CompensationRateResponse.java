package com.github.marcelorodrigo.dutytracker.usecase.response.compensation;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import java.math.BigDecimal;
import java.time.LocalTime;

public record CompensationRateResponse(
        Long id,
        RateCategory rateCategory,
        OvertimeDayType overtimeDayType,
        String label,
        LocalTime timeFrom,
        LocalTime timeTo,
        BigDecimal percentage) {}
