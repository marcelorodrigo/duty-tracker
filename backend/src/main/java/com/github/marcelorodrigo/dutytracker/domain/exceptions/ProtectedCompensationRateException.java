package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class ProtectedCompensationRateException extends RuntimeException {

    private final Long compensationRateId;

    public ProtectedCompensationRateException(Long compensationRateId) {
        super("Compensation rate " + compensationRateId
                + " is protected and cannot be deleted; only OVERTIME_ALLOWANCE rates may be deleted");
        this.compensationRateId = compensationRateId;
    }

    public Long compensationRateId() {
        return compensationRateId;
    }
}
