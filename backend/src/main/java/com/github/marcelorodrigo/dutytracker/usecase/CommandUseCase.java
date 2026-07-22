package com.github.marcelorodrigo.dutytracker.usecase;

@FunctionalInterface
public interface CommandUseCase<R> {
    void execute(R request);
}
