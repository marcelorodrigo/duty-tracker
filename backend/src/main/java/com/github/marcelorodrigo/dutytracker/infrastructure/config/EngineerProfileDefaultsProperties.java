package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import com.github.marcelorodrigo.dutytracker.usecase.profile.EngineerProfileDefaults;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configurable defaults applied when optional values are omitted during profile creation. */
@Validated
@ConfigurationProperties(prefix = "app.profile-defaults")
public record EngineerProfileDefaultsProperties(
        @NotNull @DecimalMin("1.00") @Digits(integer = 17, fraction = 2)
        BigDecimal hourlyRate,

        @NotNull @DecimalMin("0.001") @Digits(integer = 5, fraction = 5)
        BigDecimal standbyWeekdaySaturdayPercentage,

        @NotNull @DecimalMin("0.001") @Digits(integer = 5, fraction = 5)
        BigDecimal standbyWeekdaySundayHolidayPercentage)
        implements EngineerProfileDefaults {}
