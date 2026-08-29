package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application-level configuration properties bound from {@code app.*} in application.yml.
 *
 * <p>Set {@code app.base-url} to the public-facing base URL of this service. Used to build
 * RFC 7807 problem type URIs in error responses. Example: {@code https://api.example.com}
 *
 * <p>Set {@code app.cors.allowed-origins} to configure CORS allowed origins. Defaults to {@code
 * http://localhost:3000} for local development.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@NotBlank String baseUrl, @Valid CorsProperties cors) {

    public AppProperties {
        if (cors == null) {
            cors = new CorsProperties(null);
        }
    }

    public record CorsProperties(@NotEmpty @NotBlankElements List<String> allowedOrigins) {
        public CorsProperties {
            if (allowedOrigins == null || allowedOrigins.isEmpty()) {
                allowedOrigins = List.of("http://localhost:3000");
            }
        }
    }
}
