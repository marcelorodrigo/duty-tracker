package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCompensationRateUseCase implements UseCase<DeleteCompensationRateRequest, Void> {

    private final CompensationRateGateway compensationRateGateway;

    @Override
    @Transactional
    public Void execute(DeleteCompensationRateRequest request) {
        compensationRateGateway.findById(request.rateId()).ifPresent(rate -> {
            if (rate.rateCategory() != RateCategory.OVERTIME_ALLOWANCE) {
                throw new ProfileAlreadyExistsException(
                        "Cannot delete base rate row: only OVERTIME_ALLOWANCE rows may be deleted");
            }
        });
        compensationRateGateway.deleteById(request.rateId());
        return null;
    }
}
