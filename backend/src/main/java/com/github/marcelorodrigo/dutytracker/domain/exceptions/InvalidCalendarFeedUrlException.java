package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidCalendarFeedUrlException extends RuntimeException {
    public InvalidCalendarFeedUrlException(String message) {
        super(message);
    }
}
