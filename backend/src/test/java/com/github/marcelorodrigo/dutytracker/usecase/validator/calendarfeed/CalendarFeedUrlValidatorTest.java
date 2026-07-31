package com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CalendarFeedUrlValidatorTest {

    private final CalendarFeedUrlValidator validator = new CalendarFeedUrlValidator();

    @Test
    @DisplayName("should accept a valid incident.io HTTPS URL")
    void shouldAcceptValidIncidentIoUrl() {
        assertThatNoException().isThrownBy(() -> validator.validate("https://app.incident.io/feed.ics"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("should reject blank or null-looking URLs")
    void shouldRejectBlankUrls(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    @DisplayName("should reject a null URL")
    void shouldRejectNullUrl() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("not configured");
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://app.incident.io/feed.ics", "ftp://app.incident.io/feed.ics"})
    @DisplayName("should reject non-HTTPS URLs")
    void shouldRejectNonHttpsUrls(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("HTTPS");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com/feed.ics", "https://evil.incident.io.io/feed.ics"})
    @DisplayName("should reject URLs not hosted on app.incident.io")
    void shouldRejectWrongHost(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("host must be app.incident.io");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://localhost/feed.ics",
        "https://127.0.0.1/feed.ics",
        "https://192.168.1.1/feed.ics",
        "https://10.0.0.1/feed.ics",
        "https://172.16.0.1/feed.ics",
        "https://169.254.1.1/feed.ics",
        "https://[::1]/feed.ics",
        "https://[fe80::1]/feed.ics",
        "https://[fc00::1]/feed.ics"
    })
    @DisplayName("should reject localhost and private IP URLs")
    void shouldRejectPrivateHosts(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("should reject URLs that are too long")
    void shouldRejectTooLongUrl() {
        String url = "https://app.incident.io/" + "a".repeat(2048);

        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("must not exceed 2048 characters");
    }

    @Test
    @DisplayName("should reject malformed URLs")
    void shouldRejectMalformedUrl() {
        assertThatThrownBy(() -> validator.validate("https:// app.incident.io/feed.ics"))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("not a valid URL");
    }

    @Test
    @DisplayName("should accept a URL with an app.incident.io path regardless of DNS")
    void shouldAllowIncidentIoUrlWithoutDnsLookup() {
        assertThatNoException()
                .isThrownBy(() -> validator.validate("https://app.incident.io/subscriptions/123/feed.ics"));
    }
}
