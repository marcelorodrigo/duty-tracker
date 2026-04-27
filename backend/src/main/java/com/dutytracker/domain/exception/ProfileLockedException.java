package com.dutytracker.domain.exception;

public class ProfileLockedException extends RuntimeException {
    public ProfileLockedException(String message) {
        super(message);
    }
}
