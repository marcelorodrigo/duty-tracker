package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
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
        var properties = new AppProperties("https://api.example.com");

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
        var properties = new AppProperties(baseUrl);

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
        var properties = new AppProperties(null);

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath()).hasToString("baseUrl");
    }
}
