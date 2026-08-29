package com.github.marcelorodrigo.dutytracker.domain.exceptions;

public class InvalidPaginationRequestException extends RuntimeException {

    public InvalidPaginationRequestException(String message) {
        super(message);
    }
}
