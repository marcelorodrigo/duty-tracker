package com.dutytracker.infrastructure.holiday;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JollydayPublicHolidayGatewayTest {

    private final JollydayPublicHolidayGateway gateway = new JollydayPublicHolidayGateway();

    @Test
    void koningsdagApril27IsHoliday() {
        assertThat(gateway.isHoliday(LocalDate.of(2026, 4, 27))).isTrue();
    }

    @Test
    void eersteKerstdagDec25IsHoliday() {
        assertThat(gateway.isHoliday(LocalDate.of(2025, 12, 25))).isTrue();
    }

    @Test
    void easterMondayIsHoliday() {
        // Tweede Paasdag 2026 = April 6
        assertThat(gateway.isHoliday(LocalDate.of(2026, 4, 6))).isTrue();
    }

    @Test
    void regularWednesdayIsNotHoliday() {
        assertThat(gateway.isHoliday(LocalDate.of(2026, 4, 8))).isFalse();
    }

    @Test
    void getHolidaysReturnsKoningsdag() {
        var holidays = gateway.getHolidays(2026);
        assertThat(holidays).contains(LocalDate.of(2026, 4, 27));
    }
}
