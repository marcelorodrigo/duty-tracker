package com.dutytracker.application.usecase.compensation;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.ProfileAlreadyExistsException;
import com.dutytracker.domain.gateway.CompensationRateGateway;
import com.dutytracker.domain.model.RateCategory;
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
