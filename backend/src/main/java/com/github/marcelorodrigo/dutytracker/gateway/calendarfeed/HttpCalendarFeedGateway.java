package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedAuthenticationException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class HttpCalendarFeedGateway implements CalendarFeedGateway {

    public static final int MAX_BODY_SIZE_BYTES = 1024 * 1024;
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;

    public HttpCalendarFeedGateway() {
        this.restClient =
                RestClient.builder().requestFactory(createRequestFactory()).build();
    }

    private ClientHttpRequestFactory createRequestFactory() {
        HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(READ_TIMEOUT);
        return factory;
    }

    @Override
    public String fetch(String url) {
        try {
            return restClient.get().uri(url).exchange((request, response) -> {
                int status = response.getStatusCode().value();
                if (status >= 300 && status < 400) {
                    throw new CalendarFeedFetchException("Calendar feed redirects are not allowed");
                }
                if (status == 401 || status == 403 || status == 404) {
                    throw new CalendarFeedAuthenticationException();
                }
                if (status >= 400 && status < 600) {
                    throw new CalendarFeedFetchException("Calendar feed upstream returned HTTP " + status);
                }
                long contentLength = response.getHeaders().getContentLength();
                if (contentLength > MAX_BODY_SIZE_BYTES) {
                    throw new CalendarFeedFetchException("Calendar feed response body exceeds maximum size");
                }
                try (InputStream body = response.getBody()) {
                    byte[] bytes = body.readNBytes(MAX_BODY_SIZE_BYTES + 1);
                    if (bytes.length > MAX_BODY_SIZE_BYTES) {
                        throw new CalendarFeedFetchException("Calendar feed response body exceeds maximum size");
                    }
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            });
        } catch (ResourceAccessException e) {
            throw new CalendarFeedFetchException("Failed to fetch calendar feed: " + e.getMessage());
        }
    }
}
