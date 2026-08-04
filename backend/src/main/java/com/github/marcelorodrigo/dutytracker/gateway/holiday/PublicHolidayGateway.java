package com.github.marcelorodrigo.dutytracker.gateway.holiday;

import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayGateway {
    List<PublicHoliday> getHolidaysWithNames(LocalDate start, LocalDate end);
}
