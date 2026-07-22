package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class CreateCompensationRateValidator implements RequestValidator<CreateCompensationRateRequest> {

    @Override
    public void validate(CreateCompensationRateRequest request) {
        if (request.overtimeDayType() == null) {
            throw new InvalidCompensationRateException("overtimeDayType is required");
        }
        if (request.timeFrom() == null || request.timeTo() == null) {
            throw new InvalidCompensationRateException("timeFrom and timeTo are required");
        }
    }
}
