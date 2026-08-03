package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class CalendarFeedNotConfiguredException extends RuntimeException {
    public CalendarFeedNotConfiguredException() {
        super("No calendar feed URL is configured in the engineer profile");
    }
}
