package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.domain.RateCategory;
import org.springframework.stereotype.Component;

@Component
public class DeleteCompensationRateValidator implements RequestValidator<DeleteCompensationRateRequest> {

    private final CompensationRateGateway compensationRateGateway;

    public DeleteCompensationRateValidator(CompensationRateGateway compensationRateGateway) {
        this.compensationRateGateway = compensationRateGateway;
    }

    @Override
    public void validate(DeleteCompensationRateRequest request) {
        compensationRateGateway.findById(request.rateId()).ifPresent(rate -> {
            if (rate.rateCategory() != RateCategory.OVERTIME_ALLOWANCE) {
                throw new ProfileAlreadyExistsException(
                        "Cannot delete base rate row: only OVERTIME_ALLOWANCE rows may be deleted");
            }
        });
    }
}
