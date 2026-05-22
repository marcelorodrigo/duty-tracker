package com.github.marcelorodrigo.dutytracker.usecase;

@FunctionalInterface
public interface UseCase<R, S> {
    S execute(R request);
}
