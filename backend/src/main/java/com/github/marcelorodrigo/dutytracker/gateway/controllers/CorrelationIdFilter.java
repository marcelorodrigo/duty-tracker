package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";
    static final String MDC_KEY = "correlationId";
    static final String REQUEST_ATTRIBUTE = CorrelationIdFilter.class.getName() + ".correlationId";

    private static final int MAXIMUM_LENGTH = 128;
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var correlationId = resolveCorrelationId(request);
        request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        MDC.put(MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    static String from(HttpServletRequest request) {
        var correlationId = request.getAttribute(REQUEST_ATTRIBUTE);
        return correlationId instanceof String value ? value : null;
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        var values = request.getHeaders(HEADER_NAME);
        if (values.hasMoreElements()) {
            var candidate = values.nextElement();
            if (!values.hasMoreElements() && isValid(candidate)) {
                return candidate;
            }
        }
        return UUID.randomUUID().toString();
    }

    private static boolean isValid(String candidate) {
        return candidate != null
                && candidate.length() <= MAXIMUM_LENGTH
                && VALID_CORRELATION_ID.matcher(candidate).matches();
    }
}
