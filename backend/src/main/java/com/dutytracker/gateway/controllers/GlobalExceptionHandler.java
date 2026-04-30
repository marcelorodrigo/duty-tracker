package com.dutytracker.gateway.controllers;

import com.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.domain.exceptions.ProfileNotFoundException;
import java.net.URI;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ProblemDetail handleProfileAlreadyExists(ProfileAlreadyExistsException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/profile-already-exists"));
        pd.setTitle("Profile already exists");
        return pd;
    }

    @ExceptionHandler(InvalidEngineerProfileException.class)
    public ProblemDetail handleInvalidEngineerProfile(InvalidEngineerProfileException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/invalid-engineer-profile"));
        pd.setTitle("Invalid engineer profile");
        return pd;
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/profile-not-found"));
        pd.setTitle("Profile not found");
        return pd;
    }

    @ExceptionHandler(InvalidOnCallPeriodException.class)
    public ProblemDetail handleInvalidOnCallPeriod(InvalidOnCallPeriodException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/invalid-oncall-period"));
        pd.setTitle("Invalid on-call period");
        return pd;
    }

    @ExceptionHandler(InvalidIncidentException.class)
    public ProblemDetail handleInvalidIncident(InvalidIncidentException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/invalid-incident"));
        pd.setTitle("Invalid incident");
        return pd;
    }

    @ExceptionHandler(HolidayAlreadyRegisteredException.class)
    public ProblemDetail handleHolidayAlreadyRegistered(HolidayAlreadyRegisteredException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/holiday-already-registered"));
        pd.setTitle("Holiday already registered");
        return pd;
    }

    @ExceptionHandler(IncidentDuringWorkingHoursException.class)
    public ProblemDetail handleIncidentDuringWorkingHours(IncidentDuringWorkingHoursException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/incident-during-working-hours"));
        pd.setTitle("Incident during working hours");
        return pd;
    }

    @ExceptionHandler(DuplicateCompensationRateException.class)
    public ProblemDetail handleDuplicateCompensationRate(DuplicateCompensationRateException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/duplicate-compensation-rate"));
        pd.setTitle("Duplicate compensation rate");
        return pd;
    }

    @ExceptionHandler(CompensationRateNotFoundException.class)
    public ProblemDetail handleCompensationRateNotFound(CompensationRateNotFoundException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("http://localhost:8080/errors/compensation-rate-not-found"));
        pd.setTitle("Compensation rate not found");
        return pd;
    }
}
