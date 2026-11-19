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
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidPaginationRequestException;
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

    private ProblemDetail clientProblem(
            Exception ex, HttpStatus status, String type, String title, HttpServletRequest request) {
        var message = "Client error: " + Character.toLowerCase(title.charAt(0)) + title.substring(1);
        return clientProblem(ex, status, type, title, message, log.atWarn(), request);
    }

    private ProblemDetail clientProblem(
            Exception ex,
            HttpStatus status,
            String type,
            String title,
            String logMessage,
            LoggingEventBuilder logEvent,
            HttpServletRequest request) {
        logEvent.addKeyValue(EXCEPTION_TYPE, ex.getClass().getSimpleName())
                .addKeyValue(DETAIL, ex.getMessage())
                .log(logMessage);
        var pd = problem(status, type, title, ex.getMessage());
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
    public ProblemDetail handleProfileAlreadyExists(ProfileAlreadyExistsException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.CONFLICT, "profile-already-exists", "Profile already exists", request);
    }

    @ExceptionHandler(InvalidEngineerProfileException.class)
    public ProblemDetail handleInvalidEngineerProfile(InvalidEngineerProfileException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.BAD_REQUEST, "invalid-engineer-profile", "Invalid engineer profile", request);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "profile-not-found", "Profile not found", request);
    }

    @ExceptionHandler(InvalidOnCallPeriodException.class)
    public ProblemDetail handleInvalidOnCallPeriod(InvalidOnCallPeriodException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-oncall-period", "Invalid on-call period", request);
    }

    @ExceptionHandler(InvalidPaginationRequestException.class)
    public ProblemDetail handleInvalidPaginationRequest(
            InvalidPaginationRequestException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.BAD_REQUEST, "invalid-pagination-request", "Invalid pagination request", request);
    }

    @ExceptionHandler(OnCallPeriodNotFoundException.class)
    public ProblemDetail handleOnCallPeriodNotFound(OnCallPeriodNotFoundException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "oncall-period-not-found", "On-call period not found", request);
    }

    @ExceptionHandler(OnCallPeriodOverlapException.class)
    public ProblemDetail handleOnCallPeriodOverlap(OnCallPeriodOverlapException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "oncall-period-overlap", "On-call period overlap", request);
    }

    @ExceptionHandler(InvalidIncidentException.class)
    public ProblemDetail handleInvalidIncident(InvalidIncidentException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-incident", "Invalid incident", request);
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ProblemDetail handleIncidentNotFound(IncidentNotFoundException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "incident-not-found", "Incident not found", request);
    }

    @ExceptionHandler(IncidentOverlapException.class)
    public ProblemDetail handleIncidentOverlap(IncidentOverlapException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.CONFLICT, "incident-overlap", "Incident overlap", request);
    }

    @ExceptionHandler(HolidayAlreadyRegisteredException.class)
    public ProblemDetail handleHolidayAlreadyRegistered(
            HolidayAlreadyRegisteredException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.CONFLICT, "holiday-already-registered", "Holiday already registered", request);
    }

    @ExceptionHandler(HolidayNotFoundException.class)
    public ProblemDetail handleHolidayNotFound(HolidayNotFoundException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.NOT_FOUND, "holiday-not-found", "Holiday not found", request);
    }

    @ExceptionHandler(IncidentDuringWorkingHoursException.class)
    public ProblemDetail handleIncidentDuringWorkingHours(
            IncidentDuringWorkingHoursException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.CONFLICT, "incident-during-working-hours", "Incident during working hours", request);
    }

    @ExceptionHandler(DuplicateCompensationRateException.class)
    public ProblemDetail handleDuplicateCompensationRate(
            DuplicateCompensationRateException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.CONFLICT, "duplicate-compensation-rate", "Duplicate compensation rate", request);
    }

    @ExceptionHandler(CompensationRateNotFoundException.class)
    public ProblemDetail handleCompensationRateNotFound(
            CompensationRateNotFoundException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.NOT_FOUND, "compensation-rate-not-found", "Compensation rate not found", request);
    }

    @ExceptionHandler(ProtectedCompensationRateException.class)
    public ProblemDetail handleProtectedCompensationRate(
            ProtectedCompensationRateException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.CONFLICT,
                "protected-compensation-rate",
                "Protected compensation rate",
                "Client error: protected compensation rate cannot be deleted",
                log.atWarn().addKeyValue("compensationRateId", ex.compensationRateId()),
                request);
    }

    @ExceptionHandler(InvalidCompensationRateException.class)
    public ProblemDetail handleInvalidCompensationRate(
            InvalidCompensationRateException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.BAD_REQUEST, "invalid-compensation-rate", "Invalid compensation rate", request);
    }

    @ExceptionHandler(InvalidHolidaySuggestionRangeException.class)
    public ProblemDetail handleInvalidHolidaySuggestionRange(
            InvalidHolidaySuggestionRangeException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.BAD_REQUEST,
                "invalid-holiday-suggestion-range",
                "Invalid holiday suggestion range",
                request);
    }

    @ExceptionHandler(InvalidHourlyRateException.class)
    public ProblemDetail handleInvalidHourlyRate(InvalidHourlyRateException ex, HttpServletRequest request) {
        return clientProblem(ex, HttpStatus.BAD_REQUEST, "invalid-hourly-rate", "Invalid hourly rate", request);
    }

    @ExceptionHandler(InvalidStandbyPercentageException.class)
    public ProblemDetail handleInvalidStandbyPercentage(
            InvalidStandbyPercentageException ex, HttpServletRequest request) {
        return clientProblem(
                ex, HttpStatus.BAD_REQUEST, "invalid-standby-percentage", "Invalid standby percentage", request);
    }

    @ExceptionHandler(CalendarFeedNotConfiguredException.class)
    public ProblemDetail handleCalendarFeedNotConfigured(
            CalendarFeedNotConfiguredException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.NOT_FOUND,
                "calendar-feed-not-configured",
                "Calendar feed not configured",
                "Client error: calendar feed not configured",
                log.atWarn(),
                request);
    }

    @ExceptionHandler(InvalidCalendarFeedUrlException.class)
    public ProblemDetail handleInvalidCalendarFeedUrl(InvalidCalendarFeedUrlException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.BAD_REQUEST,
                "invalid-calendar-feed-url",
                "Invalid calendar feed URL",
                "Client error: invalid calendar feed URL",
                log.atWarn(),
                request);
    }

    @ExceptionHandler(CalendarFeedAuthenticationException.class)
    public ProblemDetail handleCalendarFeedAuthentication(
            CalendarFeedAuthenticationException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "calendar-feed-authentication",
                "Calendar feed URL rejected",
                "Client error: calendar feed URL rejected by upstream",
                log.atWarn(),
                request);
    }

    @ExceptionHandler(CalendarFeedFetchException.class)
    public ProblemDetail handleCalendarFeedFetch(CalendarFeedFetchException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.BAD_GATEWAY,
                "calendar-feed-fetch",
                "Calendar feed fetch failed",
                "Client error: calendar feed fetch failed",
                log.atWarn(),
                request);
    }

    @ExceptionHandler(CalendarFeedParseException.class)
    public ProblemDetail handleCalendarFeedParse(CalendarFeedParseException ex, HttpServletRequest request) {
        return clientProblem(
                ex,
                HttpStatus.BAD_GATEWAY,
                "calendar-feed-parse",
                "Calendar feed parse failed",
                "Client error: calendar feed parse failed",
                log.atWarn(),
                request);
    }
}
