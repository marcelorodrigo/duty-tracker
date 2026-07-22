package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private static final String REQUEST_LOG_MESSAGE = "Test request handled";

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearDiagnosticContext() {
        MDC.clear();
    }

    @Test
    @DisplayName("should propagate a valid correlation ID through the complete request lifecycle")
    void shouldPropagateValidCorrelationIdThroughRequestLifecycle() throws Exception {
        // given
        var logger = (Logger) LoggerFactory.getLogger(CorrelationIdFilterTest.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        var request = new MockHttpServletRequest("POST", "/api/v1/incidents");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "incident-request-123");
        request.addHeader("Authorization", "Bearer header-secret");
        request.setContent("{\"password\":\"body-secret\"}".getBytes(StandardCharsets.UTF_8));
        var response = new MockHttpServletResponse();
        MDC.put(CorrelationIdFilter.MDC_KEY, "stale-request-id");

        // when
        try {
            filter.doFilter(
                    request,
                    response,
                    (servletRequest, servletResponse) -> logger.atInfo()
                            .addKeyValue("requestPath", ((MockHttpServletRequest) servletRequest).getRequestURI())
                            .log(REQUEST_LOG_MESSAGE));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        // then
        assertThat(request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE)).isEqualTo("incident-request-123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("incident-request-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
        var event = appender.list.stream()
                .filter(logEvent -> REQUEST_LOG_MESSAGE.equals(logEvent.getFormattedMessage()))
                .findFirst()
                .orElseThrow();
        assertThat(event.getMDCPropertyMap()).containsEntry(CorrelationIdFilter.MDC_KEY, "incident-request-123");
        assertThat(event.getFormattedMessage()).doesNotContain("header-secret", "body-secret");
        assertThat(event.getKeyValuePairs())
                .extracting(keyValue -> String.valueOf(keyValue.value))
                .allSatisfy(value -> assertThat(value).doesNotContain("header-secret", "body-secret"));
        assertThat(event.getMDCPropertyMap().values())
                .allSatisfy(value -> assertThat(value).doesNotContain("header-secret", "body-secret"));
    }

    @Test
    @DisplayName("should generate a correlation ID when the request does not supply one")
    void shouldGenerateCorrelationIdWhenRequestDoesNotSupplyOne() throws Exception {
        // given
        var request = new MockHttpServletRequest("GET", "/api/v1/profile");
        var response = new MockHttpServletResponse();
        var requestCorrelationId = new AtomicReference<String>();

        // when
        filter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> requestCorrelationId.set(MDC.get(CorrelationIdFilter.MDC_KEY)));

        // then
        var generatedId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(generatedId).isEqualTo(requestCorrelationId.get());
        assertThat(request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE)).isEqualTo(generatedId);
        assertThat(UUID.fromString(generatedId)).isNotNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @ParameterizedTest(name = "should replace unsafe correlation ID [{index}]")
    @MethodSource("unsafeCorrelationIds")
    void shouldReplaceUnsafeCorrelationIds(String unsafeCorrelationId) throws Exception {
        // given
        var request = new MockHttpServletRequest("GET", "/api/v1/profile");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, unsafeCorrelationId);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {});

        // then
        var generatedId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(generatedId).isNotEqualTo(unsafeCorrelationId);
        assertThat(UUID.fromString(generatedId)).isNotNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("should replace multiple correlation ID headers")
    void shouldReplaceMultipleCorrelationIdHeaders() throws Exception {
        // given
        var request = new MockHttpServletRequest("GET", "/api/v1/profile");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "first-request-id");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "second-request-id");
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {});

        // then
        var generatedId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(generatedId).isNotIn("first-request-id", "second-request-id");
        assertThat(UUID.fromString(generatedId)).isNotNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("should clear the correlation ID when request processing fails")
    void shouldClearCorrelationIdWhenRequestProcessingFails() {
        // given
        var request = new MockHttpServletRequest("GET", "/api/v1/profile");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "failing-request-123");
        var response = new MockHttpServletResponse();

        // when / then
        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
                    throw new ServletException("Request failed");
                }))
                .isInstanceOf(ServletException.class);
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("failing-request-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    private static Stream<String> unsafeCorrelationIds() {
        return Stream.of("contains whitespace", "line\nbreak", "a".repeat(129));
    }
}
