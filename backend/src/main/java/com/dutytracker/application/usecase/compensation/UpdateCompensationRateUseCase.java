package com.dutytracker.application.usecase.compensation;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.CompensationRateGateway;
import com.dutytracker.domain.model.CompensationRate;
import org.springframework.stereotype.Service;

@Service
public class UpdateCompensationRateUseCase implements UseCase<UpdateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final UpdateCompensationRateValidator validator;

    public UpdateCompensationRateUseCase(CompensationRateGateway compensationRateGateway,
                                         UpdateCompensationRateValidator validator) {
        this.compensationRateGateway = compensationRateGateway;
        this.validator = validator;
    }

    @Override
    public CompensationRateResponse execute(UpdateCompensationRateRequest request) {
        validator.validate(request);
        CompensationRate existing = compensationRateGateway.findById(request.rateId())
                .orElseThrow(() -> new RuntimeException("Rate not found: " + request.rateId()));
        CompensationRate updated = new CompensationRate(
                existing.id(),
                existing.employeeType(),
                existing.rateCategory(),
                request.label(),
                existing.timeFrom(),
                existing.timeTo(),
                request.percentage()
        );
        CompensationRate saved = compensationRateGateway.update(updated);
        return new CompensationRateResponse(
                saved.id(), saved.employeeType(), saved.rateCategory(),
                saved.label(), saved.timeFrom(), saved.timeTo(), saved.percentage());
    }
}
