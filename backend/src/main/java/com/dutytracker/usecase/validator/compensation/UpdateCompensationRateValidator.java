package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.validator.RequestValidator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCompensationRateValidator implements RequestValidator<UpdateCompensationRateRequest> {

    private final CompensationRateGateway compensationRateGateway;

    @Override
    public void validate(UpdateCompensationRateRequest request) {
        if (request.percentage() == null || request.percentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("percentage must be >= 0");
        }
        compensationRateGateway
                .findById(request.rateId())
                .orElseThrow(() -> new RuntimeException("Rate not found: " + request.rateId()));
    }
}
