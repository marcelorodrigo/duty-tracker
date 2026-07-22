package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import org.hibernate.exception.ConstraintViolationException;

public final class ConstraintViolationDetector {

    private ConstraintViolationDetector() {}

    public static boolean causedBy(Throwable exception, String constraintName) {
        var cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && constraintName.equals(violation.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
