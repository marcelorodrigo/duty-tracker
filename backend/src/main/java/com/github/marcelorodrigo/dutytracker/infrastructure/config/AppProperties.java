package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application-level configuration properties bound from {@code app.*} in application.yml.
 *
 * <p>Set {@code app.base-url} to the public-facing base URL of this service. Used to build
 * RFC 7807 problem type URIs in error responses. Example: {@code https://api.example.com}
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@NotBlank String baseUrl) {}
