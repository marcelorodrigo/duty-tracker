package com.dutytracker.application.usecase;

@FunctionalInterface
public interface RequestValidator<Req> {
    void validate(Req request);
}
