package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
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
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProtectedCompensationRateException;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private static final String TEST_BASE_URL = "https://api.example.com";

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(new AppProperties(TEST_BASE_URL));
    }

    private static void assertProblemDetail(
            ProblemDetail problem, int status, String title, String detail, String type) {
        assertThat(problem.getStatus()).isEqualTo(status);
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getDetail()).isEqualTo(detail);
        assertThat(problem.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/" + type));
    }

    @Test
    @DisplayName("should return 400 with request instance for method argument validation")
    void shouldReturn400ForMethodArgumentValidation() {
        // given
        var exception = new IllegalArgumentException("Invalid request value");
        var request = new MockHttpServletRequest("POST", "/api/v1/incidents");

        // when
        var pd = handler.handleMethodArgumentValidation(exception, request);

        // then
        assertProblemDetail(
                pd,
                400,
                "Request validation failed",
                "One or more request values are invalid.",
                "request-validation-failed");
        assertThat(pd.getInstance()).isEqualTo(URI.create("/api/v1/incidents"));
    }

    @Test
    @DisplayName("should return 400 with request instance for constraint violation")
    void shouldReturn400ForConstraintViolation() {
        // given
        var exception = new ConstraintViolationException(Set.of());
        var request = new MockHttpServletRequest("GET", "/api/v1/incidents");

        // when
        var pd = handler.handleConstraintViolation(exception, request);

        // then
        assertProblemDetail(
                pd,
                400,
                "Request constraint violation",
                "One or more request constraints were violated.",
                "constraint-violation");
        assertThat(pd.getInstance()).isEqualTo(URI.create("/api/v1/incidents"));
    }

    @Test
    @DisplayName("should return 400 with request instance for malformed request")
    void shouldReturn400ForMalformedRequest() {
        // given
        var exception = new HttpMessageNotReadableException("Malformed body", new MockHttpInputMessage(new byte[0]));
        var request = new MockHttpServletRequest("POST", "/api/v1/incidents");

        // when
        var pd = handler.handleMalformedRequest(exception, request);

        // then
        assertProblemDetail(
                pd, 400, "Malformed request body", "The request body is malformed or unreadable.", "malformed-request");
        assertThat(pd.getInstance()).isEqualTo(URI.create("/api/v1/incidents"));
    }

    @Test
    @DisplayName("should return sanitized 500 with request instance for unexpected exception")
    void shouldReturn500ForUnexpectedException() {
        // given
        var exception = new IllegalStateException("Sensitive failure detail");
        var request = new MockHttpServletRequest("GET", "/api/v1/incidents");

        // when
        var pd = handler.handleUnexpectedException(exception, request);

        // then
        assertProblemDetail(pd, 500, "Internal server error", "An unexpected error occurred.", "internal-server-error");
        assertThat(pd.getInstance()).isEqualTo(URI.create("/api/v1/incidents"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for profile already exists")
    void shouldReturn409ForProfileAlreadyExists() {
        // given
        var ex = new ProfileAlreadyExistsException("Profile exists");

        // when
        var pd = handler.handleProfileAlreadyExists(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Profile already exists");
        assertThat(pd.getDetail()).isEqualTo("Profile exists");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/profile-already-exists"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid engineer profile")
    void shouldReturn400ForInvalidEngineerProfile() {
        // given
        var ex = new InvalidEngineerProfileException("Invalid profile data");

        // when
        var pd = handler.handleInvalidEngineerProfile(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid engineer profile");
        assertThat(pd.getDetail()).isEqualTo("Invalid profile data");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-engineer-profile"));
    }

    @Test
    @DisplayName("should return 404 with configured type URI for profile not found")
    void shouldReturn404ForProfileNotFound() {
        // given
        var ex = new ProfileNotFoundException();

        // when
        var pd = handler.handleProfileNotFound(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("Profile not found");
        assertThat(pd.getDetail()).isEqualTo("No engineer profile found to delete");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/profile-not-found"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid on-call period")
    void shouldReturn400ForInvalidOnCallPeriod() {
        // given
        var ex = new InvalidOnCallPeriodException("Invalid period");

        // when
        var pd = handler.handleInvalidOnCallPeriod(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid on-call period");
        assertThat(pd.getDetail()).isEqualTo("Invalid period");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-oncall-period"));
    }

    @Test
    @DisplayName("should return 404 with configured type URI for on-call period not found")
    void shouldReturn404ForOnCallPeriodNotFound() {
        // given
        var ex = new OnCallPeriodNotFoundException(42L);

        // when
        var pd = handler.handleOnCallPeriodNotFound(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("On-call period not found");
        assertThat(pd.getDetail()).isEqualTo("On-call period not found: 42");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/oncall-period-not-found"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for on-call period overlap")
    void shouldReturn400ForOnCallPeriodOverlap() {
        // given
        var ex = new OnCallPeriodOverlapException();

        // when
        var pd = handler.handleOnCallPeriodOverlap(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("On-call period overlap");
        assertThat(pd.getDetail()).isEqualTo("The requested period overlaps with an existing on-call period.");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/oncall-period-overlap"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid incident")
    void shouldReturn400ForInvalidIncident() {
        // given
        var ex = new InvalidIncidentException("Invalid incident data");

        // when
        var pd = handler.handleInvalidIncident(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid incident");
        assertThat(pd.getDetail()).isEqualTo("Invalid incident data");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-incident"));
    }

    @Test
    @DisplayName("should return 404 with configured type URI for incident not found")
    void shouldReturn404ForIncidentNotFound() {
        // given
        var ex = new IncidentNotFoundException(42L);

        // when
        var pd = handler.handleIncidentNotFound(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("Incident not found");
        assertThat(pd.getDetail()).isEqualTo("Incident not found: 42");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/incident-not-found"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for incident overlap")
    void shouldReturn409ForIncidentOverlap() {
        // given
        var ex = new IncidentOverlapException();

        // when
        var pd = handler.handleIncidentOverlap(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Incident overlap");
        assertThat(pd.getDetail()).isEqualTo("Incident overlaps with an existing incident in the same on-call period");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/incident-overlap"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for holiday already registered")
    void shouldReturn409ForHolidayAlreadyRegistered() {
        // given
        var ex = new HolidayAlreadyRegisteredException();

        // when
        var pd = handler.handleHolidayAlreadyRegistered(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Holiday already registered");
        assertThat(pd.getDetail()).isEqualTo("Holiday already registered for this date");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/holiday-already-registered"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for incident during working hours")
    void shouldReturn409ForIncidentDuringWorkingHours() {
        // given
        var ex = new IncidentDuringWorkingHoursException();

        // when
        var pd = handler.handleIncidentDuringWorkingHours(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Incident during working hours");
        assertThat(pd.getDetail()).isEqualTo("All hours fall within working hours");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/incident-during-working-hours"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for duplicate compensation rate")
    void shouldReturn409ForDuplicateCompensationRate() {
        // given
        var ex = new DuplicateCompensationRateException("Rate already exists");

        // when
        var pd = handler.handleDuplicateCompensationRate(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Duplicate compensation rate");
        assertThat(pd.getDetail()).isEqualTo("Rate already exists");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/duplicate-compensation-rate"));
    }

    @Test
    @DisplayName("should return 404 with configured type URI for compensation rate not found")
    void shouldReturn404ForCompensationRateNotFound() {
        // given
        var ex = new CompensationRateNotFoundException("Rate not found");

        // when
        var pd = handler.handleCompensationRateNotFound(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getTitle()).isEqualTo("Compensation rate not found");
        assertThat(pd.getDetail()).isEqualTo("Rate not found");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/compensation-rate-not-found"));
    }

    @Test
    @DisplayName("should return 409 with configured type URI for protected compensation rate")
    void shouldReturn409ForProtectedCompensationRate() {
        // given
        var ex = new ProtectedCompensationRateException(7L);

        // when
        var pd = handler.handleProtectedCompensationRate(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getTitle()).isEqualTo("Protected compensation rate");
        assertThat(pd.getDetail())
                .isEqualTo(
                        "Compensation rate 7 is protected and cannot be deleted; only OVERTIME_ALLOWANCE rates may be deleted");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/protected-compensation-rate"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid compensation rate")
    void shouldReturn400ForInvalidCompensationRate() {
        // given
        var ex = new InvalidCompensationRateException("Invalid rate");

        // when
        var pd = handler.handleInvalidCompensationRate(ex);

        // then
        assertProblemDetail(pd, 400, "Invalid compensation rate", "Invalid rate", "invalid-compensation-rate");
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid holiday suggestion range")
    void shouldReturn400ForInvalidHolidaySuggestionRange() {
        // given
        var ex = new InvalidHolidaySuggestionRangeException("Invalid range");

        // when
        var pd = handler.handleInvalidHolidaySuggestionRange(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid holiday suggestion range");
        assertThat(pd.getDetail()).isEqualTo("Invalid range");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-holiday-suggestion-range"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid hourly rate")
    void shouldReturn400ForInvalidHourlyRate() {
        // given
        var ex = new InvalidHourlyRateException();

        // when
        var pd = handler.handleInvalidHourlyRate(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid hourly rate");
        assertThat(pd.getDetail()).isEqualTo("Hourly rate must be greater than 1");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-hourly-rate"));
    }

    @Test
    @DisplayName("should return 400 with configured type URI for invalid standby percentage")
    void shouldReturn400ForInvalidStandbyPercentage() {
        // given
        var ex = new InvalidStandbyPercentageException("Invalid percentage");

        // when
        var pd = handler.handleInvalidStandbyPercentage(ex);

        // then
        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getTitle()).isEqualTo("Invalid standby percentage");
        assertThat(pd.getDetail()).isEqualTo("Invalid percentage");
        assertThat(pd.getType()).isEqualTo(URI.create(TEST_BASE_URL + "/errors/invalid-standby-percentage"));
    }

    @Test
    @DisplayName("should log unexpected errors with structured request context")
    void shouldLogUnexpectedErrorsWithStructuredRequestContext() {
        // given
        var logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        var request = new MockHttpServletRequest("GET", "/api/v1/incidents");
        request.addHeader("X-Correlation-ID", "incident-list-123");
        var exception = new IllegalStateException("database password=super-secret");

        // when
        try {
            handler.handleUnexpectedException(exception, request);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        // then
        var event = appender.list.stream()
                .filter(logEvent -> "Unexpected error while handling request".equals(logEvent.getFormattedMessage()))
                .findFirst()
                .orElseThrow();
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).satisfies(throwable -> {
            assertThat(throwable.getClassName()).endsWith("GlobalExceptionHandler$SanitizedDiagnosticException");
            assertThat(throwable.getMessage()).isNull();
            assertThat(throwable.getCause()).isNull();
            assertThat(throwable.getStackTraceElementProxyArray()[0].getStackTraceElement())
                    .isEqualTo(exception.getStackTrace()[0]);
        });
        assertThat(event.getKeyValuePairs())
                .extracting(keyValue -> keyValue.key, keyValue -> keyValue.value)
                .contains(
                        tuple("exceptionType", "IllegalStateException"),
                        tuple("correlationId", "incident-list-123"),
                        tuple("httpMethod", "GET"),
                        tuple("requestPath", "/api/v1/incidents"));
        assertThat(event.getKeyValuePairs())
                .extracting(keyValue -> keyValue.key)
                .contains("requestId");
        assertThat(event.getKeyValuePairs())
                .extracting(keyValue -> keyValue.value)
                .doesNotContain("database password=super-secret");
        assertThat(ThrowableProxyUtil.asString(event.getThrowableProxy()))
                .contains("shouldLogUnexpectedErrorsWithStructuredRequestContext")
                .doesNotContain("super-secret");
    }
}
