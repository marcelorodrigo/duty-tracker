package com.github.marcelorodrigo.dutytracker.gateway.holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface PublicHolidayGateway {
    boolean isHoliday(LocalDate date);

    Set<LocalDate> getHolidays(int year);

    List<PublicHoliday> getHolidaysWithNames(LocalDate start, LocalDate end);
}
