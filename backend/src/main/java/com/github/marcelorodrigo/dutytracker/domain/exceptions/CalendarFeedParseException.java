package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class CalendarFeedParseException extends RuntimeException {
    public CalendarFeedParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalendarFeedParseException(String message) {
        super(message);
    }
}
