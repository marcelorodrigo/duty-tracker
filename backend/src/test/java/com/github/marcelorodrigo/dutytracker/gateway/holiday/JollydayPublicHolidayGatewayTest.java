package com.github.marcelorodrigo.dutytracker.gateway.holiday;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JollydayPublicHolidayGatewayTest {

    private final JollydayPublicHolidayGateway gateway = new JollydayPublicHolidayGateway();

    @Test
    @DisplayName("should return Koningsdag with name when querying date range in April")
    void shouldReturnKoningsdagWithNameWhenQueryingDateRangeInApril() {
        // given
        var start = LocalDate.of(2026, 4, 1);
        var end = LocalDate.of(2026, 4, 30);

        // when
        var result = gateway.getHolidaysWithNames(start, end);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(PublicHoliday::date).contains(LocalDate.of(2026, 4, 27));
        assertThat(result)
                .filteredOn(h -> h.date().equals(LocalDate.of(2026, 4, 27)))
                .extracting(PublicHoliday::name)
                .isNotEmpty();
    }

    @Test
    @DisplayName("should return empty list when no holidays exist in the given range")
    void shouldReturnEmptyListWhenNoHolidaysExistInTheGivenRange() {
        // given
        var start = LocalDate.of(2026, 3, 9);
        var end = LocalDate.of(2026, 3, 20);

        // when
        var result = gateway.getHolidaysWithNames(start, end);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should not return holidays outside the requested date range")
    void shouldNotReturnHolidaysOutsideTheRequestedDateRange() {
        // given
        var start = LocalDate.of(2026, 1, 1);
        var end = LocalDate.of(2026, 3, 31);

        // when
        var result = gateway.getHolidaysWithNames(start, end);

        // then - Koningsdag (April 27) must not appear
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(PublicHoliday::date).doesNotContain(LocalDate.of(2026, 4, 27));
    }
}
