package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

class EngineerProfileDefaultsPropertiesTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("should bind profile defaults from application configuration")
    void shouldBindProfileDefaultsFromApplicationConfiguration() {
        // given
        var environment = new MockEnvironment()
                .withProperty("app.profile-defaults.hourly-rate", "42.50")
                .withProperty("app.profile-defaults.standby-weekday-saturday-percentage", "0.071")
                .withProperty("app.profile-defaults.standby-weekday-sunday-holiday-percentage", "0.095");

        // when
        var properties = Binder.get(environment)
                .bind("app.profile-defaults", Bindable.of(EngineerProfileDefaultsProperties.class))
                .get();

        // then
        assertThat(properties.hourlyRate()).isEqualByComparingTo(new BigDecimal("42.50"));
        assertThat(properties.standbyWeekdaySaturdayPercentage()).isEqualByComparingTo(new BigDecimal("0.071"));
        assertThat(properties.standbyWeekdaySundayHolidayPercentage()).isEqualByComparingTo(new BigDecimal("0.095"));
    }

    @Test
    @DisplayName("should accept profile defaults that fit business and database constraints")
    void shouldAcceptProfileDefaultsThatFitBusinessAndDatabaseConstraints() {
        // given
        var properties = new EngineerProfileDefaultsProperties(
                new BigDecimal("1.00"), new BigDecimal("0.067"), new BigDecimal("0.084"));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("should reject profile defaults outside business and database constraints")
    void shouldRejectProfileDefaultsOutsideBusinessAndDatabaseConstraints() {
        // given
        var properties = new EngineerProfileDefaultsProperties(
                new BigDecimal("0.99"), new BigDecimal("0.0001"), new BigDecimal("123456.00000"));

        // when
        var violations = validator.validate(properties);

        // then
        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "hourlyRate", "standbyWeekdaySaturdayPercentage", "standbyWeekdaySundayHolidayPercentage");
    }
}
