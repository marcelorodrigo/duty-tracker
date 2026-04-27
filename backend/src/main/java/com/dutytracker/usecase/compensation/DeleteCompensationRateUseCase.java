package com.dutytracker.usecase.compensation;

import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCompensationRateUseCase implements UseCase<DeleteCompensationRateRequest, Void> {

    private final CompensationRateGateway compensationRateGateway;
    private final DeleteCompensationRateValidator validator;

    @Override
    public Void execute(DeleteCompensationRateRequest request) {
        validator.validate(request);
        compensationRateGateway.deleteById(request.rateId());
        return null;
    }
}
