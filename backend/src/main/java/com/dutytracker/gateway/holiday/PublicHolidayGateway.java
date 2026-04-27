package com.dutytracker.gateway.holiday;

import java.time.LocalDate;
import java.util.Set;

public interface PublicHolidayGateway {
    boolean isHoliday(LocalDate date);

    Set<LocalDate> getHolidays(int year);
}
