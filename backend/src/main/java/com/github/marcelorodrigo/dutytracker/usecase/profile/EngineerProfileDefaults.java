package com.github.marcelorodrigo.dutytracker.usecase.profile;

import java.math.BigDecimal;

public interface EngineerProfileDefaults {

    BigDecimal hourlyRate();

    BigDecimal standbyWeekdaySaturdayPercentage();

    BigDecimal standbyWeekdaySundayHolidayPercentage();
}
