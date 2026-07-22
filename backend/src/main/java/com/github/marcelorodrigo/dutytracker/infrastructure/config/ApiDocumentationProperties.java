package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Application-owned switch controlling whether API documentation endpoints are exposed. */
@ConfigurationProperties(prefix = "app.api-documentation")
public record ApiDocumentationProperties(boolean enabled) {}
