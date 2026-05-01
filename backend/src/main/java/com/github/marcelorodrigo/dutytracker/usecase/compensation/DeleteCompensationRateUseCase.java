package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.DeleteCompensationRateValidator;
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
