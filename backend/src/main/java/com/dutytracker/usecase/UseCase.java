package com.dutytracker.usecase;

@FunctionalInterface
public interface UseCase<Req, Res> {
    Res execute(Req request);
}
