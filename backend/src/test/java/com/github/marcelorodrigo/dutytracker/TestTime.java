package com.github.marcelorodrigo.dutytracker;

import static com.github.marcelorodrigo.dutytracker.configuration.BusinessClock.BUSINESS_ZONE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

public final class TestTime {

    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-05T09:15:30Z"), BUSINESS_ZONE);
    public static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.now(FIXED_CLOCK);

    private TestTime() {}
}
