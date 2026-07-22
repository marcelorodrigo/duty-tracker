package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertimeDayClassifierTest {

    private final OvertimeDayClassifier classifier = new OvertimeDayClassifier();

    @Test
    @DisplayName("should classify configured holiday as full-day Sunday or holiday overtime")
    void shouldClassifyConfiguredHolidayAsFullDaySundayOrHolidayOvertime() {
        // given
        var monday = LocalDate.of(2026, Month.APRIL, 13);

        // when
        var result = classifier.classify(monday, Set.of(monday));

        // then
        assertThat(result).isEqualTo(new OvertimeDayClassification(OvertimeDayType.SUNDAY_HOLIDAY, true));
    }

    @Test
    @DisplayName("should classify Sunday as full-day Sunday or holiday overtime")
    void shouldClassifySundayAsFullDaySundayOrHolidayOvertime() {
        // given
        var sunday = LocalDate.of(2026, Month.APRIL, 19);

        // when
        var result = classifier.classify(sunday, Set.of());

        // then
        assertThat(result).isEqualTo(new OvertimeDayClassification(OvertimeDayType.SUNDAY_HOLIDAY, true));
    }

    @Test
    @DisplayName("should classify Saturday without treating it as full-day overtime")
    void shouldClassifySaturdayWithoutTreatingItAsFullDayOvertime() {
        // given
        var saturday = LocalDate.of(2026, Month.APRIL, 18);

        // when
        var result = classifier.classify(saturday, Set.of());

        // then
        assertThat(result).isEqualTo(new OvertimeDayClassification(OvertimeDayType.SATURDAY, false));
    }

    @Test
    @DisplayName("should classify ordinary working day as weekday")
    void shouldClassifyOrdinaryWorkingDayAsWeekday() {
        // given
        var tuesday = LocalDate.of(2026, Month.APRIL, 14);

        // when
        var result = classifier.classify(tuesday, Set.of());

        // then
        assertThat(result).isEqualTo(new OvertimeDayClassification(OvertimeDayType.WEEKDAY, false));
    }
}
