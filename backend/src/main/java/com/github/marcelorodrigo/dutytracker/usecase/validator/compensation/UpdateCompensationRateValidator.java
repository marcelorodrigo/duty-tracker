package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class UpdateCompensationRateValidator implements RequestValidator<UpdateCompensationRateRequest> {

    @Override
    public void validate(UpdateCompensationRateRequest request) {
        if (request.rateId() == null) {
            throw new InvalidCompensationRateException("rateId is required");
        }
        if (request.percentage() == null || request.percentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCompensationRateException("percentage must be >= 0");
        }
    }
}
