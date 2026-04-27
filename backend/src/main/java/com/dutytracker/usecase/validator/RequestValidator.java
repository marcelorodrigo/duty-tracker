package com.dutytracker.usecase.validator;

@FunctionalInterface
public interface RequestValidator<Req> {
    void validate(Req request);
}
