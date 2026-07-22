package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ForwardedHeaderFilter;

class ResourceLocationTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("should derive and encode resource locations from the current request")
    void shouldDeriveAndEncodeResourceLocationsFromCurrentRequest() {
        // given
        var request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("api.example.test");
        request.setServerPort(443);
        request.setContextPath("/duty");
        request.setRequestURI("/duty/api/v1/resources");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        var location = ResourceLocation.fromCurrentRequest("rate / premium");

        // then
        assertThat(location).hasToString("https://api.example.test/duty/api/v1/resources/rate%20%2F%20premium");
    }

    @Test
    @DisplayName("should use the public request authority exposed by Spring forwarded-header handling")
    void shouldUsePublicRequestAuthorityExposedBySpringForwardedHeaderHandling() throws Exception {
        // given
        var request = new MockHttpServletRequest("POST", "/api/v1/resources");
        request.setServerName("backend");
        request.setServerPort(8080);
        request.addHeader("Forwarded", "host=public.example.test;proto=https");
        var location = new AtomicReference<URI>();

        // when
        new ForwardedHeaderFilter().doFilter(request, new MockHttpServletResponse(), (filteredRequest, response) -> {
            RequestContextHolder.setRequestAttributes(
                    new ServletRequestAttributes((HttpServletRequest) filteredRequest));
            location.set(ResourceLocation.fromCurrentRequest("created"));
        });

        // then
        assertThat(location.get()).hasToString("https://public.example.test/api/v1/resources/created");
    }
}
