package com.dutytracker.usecase.response.compensation;

import com.dutytracker.domain.OvertimeDayType;
import com.dutytracker.domain.RateCategory;
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
