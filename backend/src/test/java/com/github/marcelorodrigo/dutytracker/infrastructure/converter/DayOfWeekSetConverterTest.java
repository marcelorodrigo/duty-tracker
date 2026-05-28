package com.github.marcelorodrigo.dutytracker.infrastructure.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DayOfWeekSetConverterTest {

    private final DayOfWeekSetConverter converter = new DayOfWeekSetConverter();

    @Test
    @DisplayName("should convert set of DayOfWeek to sorted comma-separated string")
    void shouldConvertSetOfDayOfWeekToSortedCommaSeparatedString() {
        // given
        var days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

        // when
        var result = converter.convertToDatabaseColumn(days);

        // then
        assertThat(result).isEqualTo("FRIDAY,MONDAY");
    }

    @Test
    @DisplayName("should return empty string when set is null")
    void shouldReturnEmptyStringWhenSetIsNull() {
        // when
        var result = converter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty string when set is empty")
    void shouldReturnEmptyStringWhenSetIsEmpty() {
        // when
        var result = converter.convertToDatabaseColumn(EnumSet.noneOf(DayOfWeek.class));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should convert comma-separated string to set of DayOfWeek")
    void shouldConvertCommaSeparatedStringToSetOfDayOfWeek() {
        // given
        var dbData = "MONDAY,FRIDAY";

        // when
        var result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("should return empty set when dbData is null")
    void shouldReturnEmptySetWhenDbDataIsNull() {
        // when
        var result = converter.convertToEntityAttribute(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty set when dbData is blank")
    void shouldReturnEmptySetWhenDbDataIsBlank() {
        // when
        var result = converter.convertToEntityAttribute("  ");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should handle single day in both directions")
    void shouldHandleSingleDayInBothDirections() {
        // given
        var days = Set.of(DayOfWeek.WEDNESDAY);

        // when
        var dbValue = converter.convertToDatabaseColumn(days);
        var restored = converter.convertToEntityAttribute(dbValue);

        // then
        assertThat(restored).containsExactly(DayOfWeek.WEDNESDAY);
    }

    @Test
    @DisplayName("should handle all seven days of the week")
    void shouldHandleAllSevenDaysOfTheWeek() {
        // given
        var allDays = EnumSet.allOf(DayOfWeek.class);

        // when
        var dbValue = converter.convertToDatabaseColumn(allDays);
        var restored = converter.convertToEntityAttribute(dbValue);

        // then
        assertThat(restored).containsExactlyInAnyOrderElementsOf(allDays);
    }
}
