package com.dutytracker.gateway.controllers;



import com.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.domain.exceptions.OnboardingNotCompletedException;
import com.dutytracker.domain.exceptions.OvertimeDayOffException;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.domain.exceptions.ProfileLockedException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ProblemDetail handleProfileAlreadyExists(ProfileAlreadyExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/profile-already-exists"));
        pd.setTitle("Profile already exists");
        return pd;
    }

    @ExceptionHandler(ProfileLockedException.class)
    public ProblemDetail handleProfileLocked(ProfileLockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/profile-locked"));
        pd.setTitle("Profile is locked");
        return pd;
    }

    @ExceptionHandler(OnboardingNotCompletedException.class)
    public ProblemDetail handleOnboardingNotCompleted(OnboardingNotCompletedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/onboarding-incomplete"));
        pd.setTitle("Onboarding not completed");
        return pd;
    }

    @ExceptionHandler(InvalidOnCallPeriodException.class)
    public ProblemDetail handleInvalidOnCallPeriod(InvalidOnCallPeriodException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/invalid-oncall-period"));
        pd.setTitle("Invalid on-call period");
        return pd;
    }

    @ExceptionHandler(InvalidIncidentException.class)
    public ProblemDetail handleInvalidIncident(InvalidIncidentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/invalid-incident"));
        pd.setTitle("Invalid incident");
        return pd;
    }

    @ExceptionHandler(HolidayAlreadyRegisteredException.class)
    public ProblemDetail handleHolidayAlreadyRegistered(HolidayAlreadyRegisteredException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/holiday-already-registered"));
        pd.setTitle("Holiday already registered");
        return pd;
    }

    @ExceptionHandler(IncidentDuringWorkingHoursException.class)
    public ProblemDetail handleIncidentDuringWorkingHours(IncidentDuringWorkingHoursException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/incident-during-working-hours"));
        pd.setTitle("Incident during working hours");
        return pd;
    }

    @ExceptionHandler(OvertimeDayOffException.class)
    public ProblemDetail handleOvertimeDayOff(OvertimeDayOffException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://dutytracker/errors/overtime-day-off"));
        pd.setTitle("Overtime on day off");
        return pd;
    }
}
