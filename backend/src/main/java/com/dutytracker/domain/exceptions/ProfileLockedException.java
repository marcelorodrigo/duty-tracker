package com.dutytracker.domain.exceptions;

public class ProfileLockedException extends RuntimeException {
    public ProfileLockedException() {
        super("Profile cannot be updated while registration summaries exist");
    }
}
