package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class OvertimeDayClassifier {

    OvertimeDayClassification classify(LocalDate date, Set<LocalDate> holidayDates) {
        boolean fullDayOvertime = DayOfWeek.SUNDAY.equals(date.getDayOfWeek()) || holidayDates.contains(date);
        OvertimeDayType dayType;

        if (fullDayOvertime) {
            dayType = OvertimeDayType.SUNDAY_HOLIDAY;
        } else if (DayOfWeek.SATURDAY.equals(date.getDayOfWeek())) {
            dayType = OvertimeDayType.SATURDAY;
        } else {
            dayType = OvertimeDayType.WEEKDAY;
        }

        return new OvertimeDayClassification(dayType, fullDayOvertime);
    }
}
