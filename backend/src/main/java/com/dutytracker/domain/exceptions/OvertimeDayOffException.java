package com.dutytracker.domain.exceptions;

public class OvertimeDayOffException extends RuntimeException {
    public OvertimeDayOffException() {
        super("Time-for-time applies for this day");
    }
}
