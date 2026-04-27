package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UpdateOnCallPeriodValidator implements RequestValidator<UpdateOnCallPeriodRequest> {

    @Override
    public void validate(UpdateOnCallPeriodRequest request) {
        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidOnCallPeriodException("endDateTime must be after startDateTime");
        }
        Duration duration = Duration.between(request.startDateTime(), request.endDateTime());
        if (duration.toHours() < 1) {
            throw new InvalidOnCallPeriodException("Period must be at least 1 hour");
        }
    }
}
