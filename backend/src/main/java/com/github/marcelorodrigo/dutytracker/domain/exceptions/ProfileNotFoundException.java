package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("No engineer profile found to delete");
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
