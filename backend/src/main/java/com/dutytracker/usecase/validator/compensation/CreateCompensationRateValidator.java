package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.domain.RateCategory;
import org.springframework.stereotype.Component;

@Component
public class CreateCompensationRateValidator implements RequestValidator<CreateCompensationRateRequest> {

    private final CompensationRateGateway compensationRateGateway;

    public CreateCompensationRateValidator(CompensationRateGateway compensationRateGateway) {
        this.compensationRateGateway = compensationRateGateway;
    }

    @Override
    public void validate(CreateCompensationRateRequest request) {
        if (request.timeFrom() == null || request.timeTo() == null) {
            throw new IllegalArgumentException("timeFrom and timeTo are required");
        }
        boolean duplicate = compensationRateGateway.findAll().stream()
                .filter(r -> r.rateCategory() == RateCategory.OVERTIME_ALLOWANCE)
                .anyMatch(r -> r.employeeType() == request.employeeType()
                        && r.timeFrom().equals(request.timeFrom())
                        && r.timeTo().equals(request.timeTo()));
        if (duplicate) {
            throw new ProfileAlreadyExistsException(
                    "An OVERTIME_ALLOWANCE rate already exists for employeeType=" + request.employeeType()
                            + " timeFrom=" + request.timeFrom() + " timeTo=" + request.timeTo());
        }
    }
}
