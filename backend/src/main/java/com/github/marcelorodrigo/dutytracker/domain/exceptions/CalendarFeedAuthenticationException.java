package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class CalendarFeedAuthenticationException extends RuntimeException {
    public CalendarFeedAuthenticationException() {
        super("The calendar feed URL was rejected by the upstream server. Please check the URL in your profile.");
    }

    public CalendarFeedAuthenticationException(String message) {
        super(message);
    }
}
