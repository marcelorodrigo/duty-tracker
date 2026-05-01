package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOnCallPeriodValidator implements RequestValidator<CreateOnCallPeriodRequest> {

    @Override
    public void validate(CreateOnCallPeriodRequest request) {
        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidOnCallPeriodException("endDateTime must be after startDateTime");
        }
        Duration duration = Duration.between(request.startDateTime(), request.endDateTime());
        if (duration.toHours() < 1) {
            throw new InvalidOnCallPeriodException("Period must be at least 1 hour");
        }
    }
}
