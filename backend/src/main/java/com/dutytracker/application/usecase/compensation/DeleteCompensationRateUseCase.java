package com.dutytracker.application.usecase.compensation;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.CompensationRateGateway;
import org.springframework.stereotype.Service;

@Service
public class DeleteCompensationRateUseCase implements UseCase<DeleteCompensationRateRequest, Void> {

    private final CompensationRateGateway compensationRateGateway;
    private final DeleteCompensationRateValidator validator;

    public DeleteCompensationRateUseCase(CompensationRateGateway compensationRateGateway,
                                         DeleteCompensationRateValidator validator) {
        this.compensationRateGateway = compensationRateGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteCompensationRateRequest request) {
        validator.validate(request);
        compensationRateGateway.deleteById(request.rateId());
        return null;
    }
}
