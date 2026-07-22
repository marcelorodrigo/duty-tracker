package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final String EXCEPTION_TYPE = "exceptionType";
    private static final String DETAIL = "detail";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final AppProperties appProperties;

    private URI errorTypeUri(String path) {
        return URI.create(appProperties.baseUrl() + "/errors/" + path);
    }

    private ProblemDetail frameworkProblem(
            HttpStatus status, String type, String title, String detail, HttpServletRequest request) {
        val pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(errorTypeUri(type));
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ProblemDetail handleMethodArgumentValidation(Exception ex, HttpServletRequest request) {
        return frameworkProblem(
                HttpStatus.BAD_REQUEST,
                "request-validation-failed",
                "Request validation failed",
                "One or more request values are invalid.",
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return frameworkProblem(
                HttpStatus.BAD_REQUEST,
                "constraint-violation",
                "Request constraint violation",
                "One or more request constraints were violated.",
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return frameworkProblem(
                HttpStatus.BAD_REQUEST,
                "malformed-request",
                "Malformed request body",
                "The request body is malformed or unreadable.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.atError()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue("requestId", request.getRequestId())
                .addKeyValue("correlationId", request.getHeader(CORRELATION_ID_HEADER))
                .addKeyValue("httpMethod", request.getMethod())
                .addKeyValue("requestPath", request.getRequestURI())
                .log("Unexpected error while handling request");
        return frameworkProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-server-error",
                "Internal server error",
                "An unexpected error occurred.",
                request);
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ProblemDetail handleProfileAlreadyExists(ProfileAlreadyExistsException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(errorTypeUri("profile-already-exists"));
        pd.setTitle("Profile already exists");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: profile already exists");
        return pd;
    }

    @ExceptionHandler(InvalidEngineerProfileException.class)
    public ProblemDetail handleInvalidEngineerProfile(InvalidEngineerProfileException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-engineer-profile"));
        pd.setTitle("Invalid engineer profile");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid engineer profile");
        return pd;
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(errorTypeUri("profile-not-found"));
        pd.setTitle("Profile not found");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: profile not found");
        return pd;
    }

    @ExceptionHandler(InvalidOnCallPeriodException.class)
    public ProblemDetail handleInvalidOnCallPeriod(InvalidOnCallPeriodException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(errorTypeUri("invalid-oncall-period"));
        pd.setTitle("Invalid on-call period");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid on-call period");
        return pd;
    }

    @ExceptionHandler(OnCallPeriodOverlapException.class)
    public ProblemDetail handleOnCallPeriodOverlap(OnCallPeriodOverlapException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("oncall-period-overlap"));
        pd.setTitle("On-call period overlap");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: on-call period overlap");
        return pd;
    }

    @ExceptionHandler(InvalidIncidentException.class)
    public ProblemDetail handleInvalidIncident(InvalidIncidentException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-incident"));
        pd.setTitle("Invalid incident");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid incident");
        return pd;
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ProblemDetail handleIncidentNotFound(IncidentNotFoundException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(errorTypeUri("incident-not-found"));
        pd.setTitle("Incident not found");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: incident not found");
        return pd;
    }

    @ExceptionHandler(IncidentOverlapException.class)
    public ProblemDetail handleIncidentOverlap(IncidentOverlapException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(errorTypeUri("incident-overlap"));
        pd.setTitle("Incident overlap");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: incident overlap");
        return pd;
    }

    @ExceptionHandler(HolidayAlreadyRegisteredException.class)
    public ProblemDetail handleHolidayAlreadyRegistered(HolidayAlreadyRegisteredException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(errorTypeUri("holiday-already-registered"));
        pd.setTitle("Holiday already registered");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: holiday already registered");
        return pd;
    }

    @ExceptionHandler(IncidentDuringWorkingHoursException.class)
    public ProblemDetail handleIncidentDuringWorkingHours(IncidentDuringWorkingHoursException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(errorTypeUri("incident-during-working-hours"));
        pd.setTitle("Incident during working hours");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: incident during working hours");
        return pd;
    }

    @ExceptionHandler(DuplicateCompensationRateException.class)
    public ProblemDetail handleDuplicateCompensationRate(DuplicateCompensationRateException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(errorTypeUri("duplicate-compensation-rate"));
        pd.setTitle("Duplicate compensation rate");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: duplicate compensation rate");
        return pd;
    }

    @ExceptionHandler(CompensationRateNotFoundException.class)
    public ProblemDetail handleCompensationRateNotFound(CompensationRateNotFoundException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(errorTypeUri("compensation-rate-not-found"));
        pd.setTitle("Compensation rate not found");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: compensation rate not found");
        return pd;
    }

    @ExceptionHandler(InvalidCompensationRateException.class)
    public ProblemDetail handleInvalidCompensationRate(InvalidCompensationRateException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-compensation-rate"));
        pd.setTitle("Invalid compensation rate");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid compensation rate");
        return pd;
    }

    @ExceptionHandler(InvalidHolidaySuggestionRangeException.class)
    public ProblemDetail handleInvalidHolidaySuggestionRange(InvalidHolidaySuggestionRangeException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-holiday-suggestion-range"));
        pd.setTitle("Invalid holiday suggestion range");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid holiday suggestion range");
        return pd;
    }

    @ExceptionHandler(InvalidHourlyRateException.class)
    public ProblemDetail handleInvalidHourlyRate(InvalidHourlyRateException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-hourly-rate"));
        pd.setTitle("Invalid hourly rate");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid hourly rate");
        return pd;
    }

    @ExceptionHandler(InvalidStandbyPercentageException.class)
    public ProblemDetail handleInvalidStandbyPercentage(InvalidStandbyPercentageException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-standby-percentage"));
        pd.setTitle("Invalid standby percentage");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid standby percentage");
        return pd;
    }
}
