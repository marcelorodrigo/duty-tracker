package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteCompensationRateValidator implements RequestValidator<DeleteCompensationRateRequest> {

    @Override
    public void validate(DeleteCompensationRateRequest request) {}
}
