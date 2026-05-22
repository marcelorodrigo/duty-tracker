package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteCompensationRateValidator implements RequestValidator<DeleteCompensationRateRequest> {

    private final CompensationRateGateway compensationRateGateway;

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
