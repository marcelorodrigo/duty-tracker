package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedAuthenticationException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedNotConfiguredException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedParseException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProtectedCompensationRateException;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.io.Serial;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.spi.LoggingEventBuilder;
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

    private static final class SanitizedDiagnosticException extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 1L;
    }

    private static final String EXCEPTION_TYPE = "exceptionType";
    private static final String DETAIL = "detail";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private final AppProperties appProperties;

    private URI errorTypeUri(String path) {
        return URI.create(appProperties.baseUrl() + "/errors/" + path);
    }

    private static Throwable sanitizedDiagnostic(Exception ex) {
        var diagnostic = new SanitizedDiagnosticException();
        diagnostic.setStackTrace(ex.getStackTrace());
        return diagnostic;
    }

    private ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        val pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(errorTypeUri(type));
        pd.setTitle(title);
        return pd;
    }

    private ProblemDetail frameworkProblem(
            HttpStatus status, String type, String title, String detail, HttpServletRequest request) {
        val pd = problem(status, type, title, detail);
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    private ProblemDetail clientProblem(Exception ex, HttpStatus status, String type, String title) {
        var message = "Client error: " + Character.toLowerCase(title.charAt(0)) + title.substring(1);
        return clientProblem(ex, status, type, title, message, log.atWarn());
    }

    private ProblemDetail clientProblem(
            Exception ex,
            HttpStatus status,
            String type,
            String title,
            String logMessage,
            LoggingEventBuilder logEvent) {
        logEvent.addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log(logMessage);
        return problem(status, type, title, ex.getMessage());
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
                .setCause(sanitizedDiagnostic(ex))
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
        return clientProblem(ex, HttpStatus.CONFLICT, "profile-already-exists", "Profile already exists");
    }

    @ExceptionHandler(InvalidEngineerProfileException.class)
    public ProblemDetail handleInvalidEngineerProfile(InvalidEngineerProfileException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-engineer-profile", "Invalid engineer profile");
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "profile-not-found", "Profile not found");
    }

    @ExceptionHandler(InvalidOnCallPeriodException.class)
    public ProblemDetail handleInvalidOnCallPeriod(InvalidOnCallPeriodException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-oncall-period", "Invalid on-call period");
    }

    @ExceptionHandler(OnCallPeriodNotFoundException.class)
    public ProblemDetail handleOnCallPeriodNotFound(OnCallPeriodNotFoundException ex) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "oncall-period-not-found", "On-call period not found");
    }

    @ExceptionHandler(OnCallPeriodOverlapException.class)
    public ProblemDetail handleOnCallPeriodOverlap(OnCallPeriodOverlapException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "oncall-period-overlap", "On-call period overlap");
    }

    @ExceptionHandler(InvalidIncidentException.class)
    public ProblemDetail handleInvalidIncident(InvalidIncidentException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-incident", "Invalid incident");
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ProblemDetail handleIncidentNotFound(IncidentNotFoundException ex) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "incident-not-found", "Incident not found");
    }

    @ExceptionHandler(IncidentOverlapException.class)
    public ProblemDetail handleIncidentOverlap(IncidentOverlapException ex) {
        return clientProblem(ex, HttpStatus.CONFLICT, "incident-overlap", "Incident overlap");
    }

    @ExceptionHandler(HolidayAlreadyRegisteredException.class)
    public ProblemDetail handleHolidayAlreadyRegistered(HolidayAlreadyRegisteredException ex) {
        return clientProblem(ex, HttpStatus.CONFLICT, "holiday-already-registered", "Holiday already registered");
    }

    @ExceptionHandler(HolidayNotFoundException.class)
    public ProblemDetail handleHolidayNotFound(HolidayNotFoundException ex, HttpServletRequest request) {
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: holiday not found");
        var pd = problem(HttpStatus.NOT_FOUND, "holiday-not-found", "Holiday not found", ex.getMessage());
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    @ExceptionHandler(IncidentDuringWorkingHoursException.class)
    public ProblemDetail handleIncidentDuringWorkingHours(IncidentDuringWorkingHoursException ex) {
        return clientProblem(ex, HttpStatus.CONFLICT, "incident-during-working-hours", "Incident during working hours");
    }

    @ExceptionHandler(DuplicateCompensationRateException.class)
    public ProblemDetail handleDuplicateCompensationRate(DuplicateCompensationRateException ex) {
        return clientProblem(ex, HttpStatus.CONFLICT, "duplicate-compensation-rate", "Duplicate compensation rate");
    }

    @ExceptionHandler(CompensationRateNotFoundException.class)
    public ProblemDetail handleCompensationRateNotFound(CompensationRateNotFoundException ex) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "compensation-rate-not-found", "Compensation rate not found");
    }

    @ExceptionHandler(ProtectedCompensationRateException.class)
    public ProblemDetail handleProtectedCompensationRate(ProtectedCompensationRateException ex) {
        return clientProblem(
                ex,
                HttpStatus.CONFLICT,
                "protected-compensation-rate",
                "Protected compensation rate",
                "Client error: protected compensation rate cannot be deleted",
                log.atWarn().addKeyValue("compensationRateId", ex.compensationRateId()));
    }

    @ExceptionHandler(InvalidCompensationRateException.class)
    public ProblemDetail handleInvalidCompensationRate(InvalidCompensationRateException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-compensation-rate", "Invalid compensation rate");
    }

    @ExceptionHandler(InvalidHolidaySuggestionRangeException.class)
    public ProblemDetail handleInvalidHolidaySuggestionRange(InvalidHolidaySuggestionRangeException ex) {
        return clientProblem(
                ex, HttpStatus.BAD_REQUEST, "invalid-holiday-suggestion-range", "Invalid holiday suggestion range");
    }

    @ExceptionHandler(InvalidHourlyRateException.class)
    public ProblemDetail handleInvalidHourlyRate(InvalidHourlyRateException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-hourly-rate", "Invalid hourly rate");
    }

    @ExceptionHandler(InvalidStandbyPercentageException.class)
    public ProblemDetail handleInvalidStandbyPercentage(InvalidStandbyPercentageException ex) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-standby-percentage", "Invalid standby percentage");
    }

    @ExceptionHandler(CalendarFeedNotConfiguredException.class)
    public ProblemDetail handleCalendarFeedNotConfigured(CalendarFeedNotConfiguredException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(errorTypeUri("calendar-feed-not-configured"));
        pd.setTitle("Calendar feed not configured");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: calendar feed not configured");
        return pd;
    }

    @ExceptionHandler(InvalidCalendarFeedUrlException.class)
    public ProblemDetail handleInvalidCalendarFeedUrl(InvalidCalendarFeedUrlException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(errorTypeUri("invalid-calendar-feed-url"));
        pd.setTitle("Invalid calendar feed URL");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: invalid calendar feed URL");
        return pd;
    }

    @ExceptionHandler(CalendarFeedAuthenticationException.class)
    public ProblemDetail handleCalendarFeedAuthentication(CalendarFeedAuthenticationException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(errorTypeUri("calendar-feed-authentication"));
        pd.setTitle("Calendar feed URL rejected");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: calendar feed URL rejected by upstream");
        return pd;
    }

    @ExceptionHandler(CalendarFeedFetchException.class)
    public ProblemDetail handleCalendarFeedFetch(CalendarFeedFetchException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        pd.setType(errorTypeUri("calendar-feed-fetch"));
        pd.setTitle("Calendar feed fetch failed");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: calendar feed fetch failed");
        return pd;
    }

    @ExceptionHandler(CalendarFeedParseException.class)
    public ProblemDetail handleCalendarFeedParse(CalendarFeedParseException ex) {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        pd.setType(errorTypeUri("calendar-feed-parse"));
        pd.setTitle("Calendar feed parse failed");
        log.atWarn()
                .addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log("Client error: calendar feed parse failed");
        return pd;
    }
}
