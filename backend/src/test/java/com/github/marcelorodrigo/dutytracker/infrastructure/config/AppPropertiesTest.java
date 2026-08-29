package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AppPropertiesTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("should pass validation when base URL is a non-blank value")
    void shouldPassValidationWhenBaseUrlIsSet() {
        // given
        var properties = new AppProperties(
                "https://api.example.com", new AppProperties.CorsProperties(List.of("http://localhost:3000")));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("should fail validation when base URL is blank or empty")
    @ValueSource(strings = {"", "   "})
    void shouldFailValidationWhenBaseUrlIsBlank(String baseUrl) {
        // given
        var properties = new AppProperties(baseUrl, new AppProperties.CorsProperties(List.of("http://localhost:3000")));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath()).hasToString("baseUrl");
    }

    @Test
    @DisplayName("should fail validation when base URL is null")
    void shouldFailValidationWhenBaseUrlIsNull() {
        // given
        var properties = new AppProperties(null, new AppProperties.CorsProperties(List.of("http://localhost:3000")));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath()).hasToString("baseUrl");
    }

    @Test
    @DisplayName("should default CORS allowed origins to localhost:3000 when not provided")
    void shouldDefaultCorsOriginsWhenNotProvided() {
        // given
        var cors = new AppProperties.CorsProperties(null);

        // then
        assertThat(cors.allowedOrigins()).containsExactly("http://localhost:3000");
    }

    @Test
    @DisplayName("should default CORS allowed origins to localhost:3000 when empty")
    void shouldDefaultCorsOriginsWhenEmpty() {
        // given
        var cors = new AppProperties.CorsProperties(List.of());

        // then
        assertThat(cors.allowedOrigins()).containsExactly("http://localhost:3000");
    }

    @Test
    @DisplayName("should fail validation when any CORS allowed origin is blank")
    void shouldFailValidationWhenCorsOriginIsBlank() {
        // given
        var properties = new AppProperties("https://api.example.com", new AppProperties.CorsProperties(List.of(" ")));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath()).hasToString("cors.allowedOrigins");
    }

    @Test
    @DisplayName("should keep configured CORS allowed origins")
    void shouldKeepConfiguredCorsOrigins() {
        // given
        var origins = List.of("https://app.example.com", "https://staging.example.com");
        var cors = new AppProperties.CorsProperties(origins);

        // then
        assertThat(cors.allowedOrigins()).isEqualTo(origins);
    }

    @Test
    @DisplayName("should default CORS origins to localhost:3000 when cors is not configured")
    void shouldDefaultCorsWhenNotConfigured() {
        // given
        var properties = new AppProperties("https://api.example.com", null);

        // then
        assertThat(properties.cors().allowedOrigins()).containsExactly("http://localhost:3000");
    }
}
