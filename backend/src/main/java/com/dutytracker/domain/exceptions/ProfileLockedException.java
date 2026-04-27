package com.dutytracker.domain.exceptions;

public class ProfileLockedException extends RuntimeException {
    public ProfileLockedException(String message) {
        super(message);
    }
}
