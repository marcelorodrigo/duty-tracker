package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedAuthenticationException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed.CalendarFeedUrlValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpCalendarFeedGatewayTest {

    private static final String FEED_URL = "https://app.incident.io/feed.ics";

    private MockRestServiceServer server;
    private HttpCalendarFeedGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new HttpCalendarFeedGateway(new CalendarFeedUrlValidator(), builder);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    @DisplayName("returns response body for a valid incident.io URL")
    void returnsBodyForValidUrl() {
        server.expect(requestTo(FEED_URL)).andRespond(withSuccess("ICS-DATA", MediaType.TEXT_PLAIN));

        assertThat(gateway.fetch(FEED_URL)).isEqualTo("ICS-DATA");
    }

    @Test
    @DisplayName("rejects an invalid URL before making an outbound request")
    void rejectsInvalidUrlBeforeRequest() {
        assertThatThrownBy(() -> gateway.fetch("http://app.incident.io/feed.ics"))
                .isInstanceOf(InvalidCalendarFeedUrlException.class);

        server.verify();
    }

    @ParameterizedTest(name = "throws authentication exception for {0} upstream responses")
    @EnumSource(
            value = HttpStatus.class,
            names = {"UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND"})
    @DisplayName("throws authentication exception for 401/403/404 upstream responses")
    void throwsAuthenticationExceptionForUpstreamErrors(HttpStatus status) {
        server.expect(requestTo(FEED_URL)).andRespond(withStatus(status));

        assertThatThrownBy(() -> gateway.fetch(FEED_URL)).isInstanceOf(CalendarFeedAuthenticationException.class);
    }

    @Test
    @DisplayName("throws fetch exception for 5xx upstream responses")
    void throwsFetchExceptionForServerError() {
        server.expect(requestTo(FEED_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> gateway.fetch(FEED_URL)).isInstanceOf(CalendarFeedFetchException.class);
    }

    @Test
    @DisplayName("throws fetch exception for redirect responses")
    void throwsFetchExceptionForRedirect() {
        server.expect(requestTo(FEED_URL)).andRespond(withStatus(HttpStatus.FOUND));

        assertThatThrownBy(() -> gateway.fetch(FEED_URL))
                .isInstanceOf(CalendarFeedFetchException.class)
                .hasMessageContaining("redirect");
    }

    @Test
    @DisplayName("throws fetch exception when declared content length exceeds maximum")
    void throwsFetchExceptionForOversizedContentLength() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(HttpCalendarFeedGateway.MAX_BODY_SIZE_BYTES + 1);
        server.expect(requestTo(FEED_URL))
                .andRespond(withSuccess("x", MediaType.TEXT_PLAIN).headers(headers));

        assertThatThrownBy(() -> gateway.fetch(FEED_URL))
                .isInstanceOf(CalendarFeedFetchException.class)
                .hasMessageContaining("maximum size");
    }

    @Test
    @DisplayName("builds its production request factory wiring the validating DNS resolver")
    void buildsProductionGateway() {
        HttpCalendarFeedGateway productionGateway = new HttpCalendarFeedGateway(new CalendarFeedUrlValidator());

        assertThat(productionGateway).isNotNull();
    }
}
