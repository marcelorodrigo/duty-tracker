package com.dutytracker.usecase.compensation;

import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
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
