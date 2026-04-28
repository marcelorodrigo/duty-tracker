package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.validator.RequestValidator;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class UpdateCompensationRateValidator implements RequestValidator<UpdateCompensationRateRequest> {

    @Override
    public void validate(UpdateCompensationRateRequest request) {
        if (request.rateId() == null) {
            throw new IllegalArgumentException("rateId is required");
        }
        if (request.percentage() == null || request.percentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("percentage must be >= 0");
        }
    }
}
