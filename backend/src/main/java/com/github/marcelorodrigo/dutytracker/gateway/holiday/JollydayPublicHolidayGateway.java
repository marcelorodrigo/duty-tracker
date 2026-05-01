package com.github.marcelorodrigo.dutytracker.gateway.holiday;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import java.time.LocalDate;
import java.time.Year;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JollydayPublicHolidayGateway implements PublicHolidayGateway {

    private final HolidayManager holidayManager;

    public JollydayPublicHolidayGateway() {
        this.holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.NETHERLANDS));
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayManager.isHoliday(date);
    }

    @Override
    public Set<LocalDate> getHolidays(int year) {
        return holidayManager.getHolidays(Year.of(year)).stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());
    }
}
