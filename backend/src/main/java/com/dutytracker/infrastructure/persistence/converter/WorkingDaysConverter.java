package com.dutytracker.infrastructure.persistence.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkingDaysConverter {

    @WritingConverter
    public static class Write implements Converter<Set<DayOfWeek>, String> {
        @Override
        public String convert(Set<DayOfWeek> days) {
            if (days == null || days.isEmpty()) {
                return "";
            }
            return days.stream()
                    .map(DayOfWeek::name)
                    .collect(Collectors.joining(","));
        }
    }

    @ReadingConverter
    public static class Read implements Converter<String, Set<DayOfWeek>> {
        @Override
        public Set<DayOfWeek> convert(String value) {
            if (value == null || value.isBlank()) {
                return Collections.emptySet();
            }
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
