package com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CalendarFeedUrlValidator {

    public static final int MAX_URL_LENGTH = 2048;
    private static final String ALLOWED_HOST = "app.incident.io";
    private static final Pattern UNIQUE_LOCAL_IPV6 = Pattern.compile("^(fc|fd)[0-9a-f]{2}:.*");

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL is not configured");
        }
        String trimmed = url.trim();
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw new InvalidCalendarFeedUrlException(
                    "Calendar feed URL must not exceed " + MAX_URL_LENGTH + " characters");
        }
        if (!trimmed.startsWith("https://")) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL must use HTTPS");
        }
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL is not a valid URL");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL is not a valid URL");
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        if (isPrivateOrLocalhost(host)) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL host is not allowed");
        }
        if (!host.equalsIgnoreCase(ALLOWED_HOST)) {
            throw new InvalidCalendarFeedUrlException("Calendar feed URL host must be " + ALLOWED_HOST);
        }
    }

    private boolean isPrivateOrLocalhost(String host) {
        String lower = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(lower)) {
            return true;
        }
        // IPv4 loopback / private / link-local
        if (lower.startsWith("127.")
                || lower.startsWith("0.")
                || lower.startsWith("10.")
                || lower.startsWith("192.168.")
                || lower.startsWith("169.254.")
                || isPrivateClassB172(lower)) {
            return true;
        }
        // IPv6 loopback / unique-local / link-local
        if ("::1".equals(lower)
                || "0:0:0:0:0:0:0:1".equals(lower)
                || UNIQUE_LOCAL_IPV6.matcher(lower).matches()
                || lower.startsWith("fe80:")) {
            return true;
        }
        return false;
    }

    private boolean isPrivateClassB172(String lower) {
        if (!lower.startsWith("172.")) {
            return false;
        }
        String[] parts = lower.split("\\.", -1);
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
