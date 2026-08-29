package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NotBlankElementsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankElements {

    String message() default "must not contain blank values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
