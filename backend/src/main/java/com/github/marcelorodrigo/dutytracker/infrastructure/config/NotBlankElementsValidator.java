package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class NotBlankElementsValidator implements ConstraintValidator<NotBlankElements, List<String>> {

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }
        return values.stream().noneMatch(value -> value == null || value.isBlank());
    }
}
