package com.github.marcelorodrigo.dutytracker.gateway.holiday;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JollydayPublicHolidayGateway implements PublicHolidayGateway {

    private final HolidayManager holidayManager;

    public JollydayPublicHolidayGateway() {
        this.holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.NETHERLANDS));
    }

    @Override
    public List<PublicHoliday> getHolidaysWithNames(LocalDate start, LocalDate end) {
        return holidayManager.getHolidays(start, end).stream()
                .map(h -> new PublicHoliday(h.getDate(), h.getDescription()))
                .toList();
    }
}
