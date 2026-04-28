package com.dutytracker.usecase.compensation;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateCompensationRateUseCase implements UseCase<UpdateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final UpdateCompensationRateValidator validator;

    @Override
    public CompensationRateResponse execute(UpdateCompensationRateRequest request) {
        validator.validate(request);
        CompensationRate existing = compensationRateGateway
                .findById(request.rateId())
                .orElseThrow(() -> new CompensationRateNotFoundException("Rate not found: " + request.rateId()));
        CompensationRate updated = new CompensationRate(
                existing.id(),
                existing.employeeType(),
                existing.rateCategory(),
                existing.overtimeDayType(),
                request.label(),
                existing.timeFrom(),
                existing.timeTo(),
                request.percentage());
        CompensationRate saved = compensationRateGateway.update(updated);
        return new CompensationRateResponse(
                saved.id(),
                saved.employeeType(),
                saved.rateCategory(),
                saved.overtimeDayType(),
                saved.label(),
                saved.timeFrom(),
                saved.timeTo(),
                saved.percentage());
    }
}
