package com.dutytracker.application.usecase;

@FunctionalInterface
public interface UseCase<Req, Res> {
    Res execute(Req request);
}
