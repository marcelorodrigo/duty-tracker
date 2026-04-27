package com.dutytracker.domain.exceptions;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("No engineer profile found to delete");
    }
}
