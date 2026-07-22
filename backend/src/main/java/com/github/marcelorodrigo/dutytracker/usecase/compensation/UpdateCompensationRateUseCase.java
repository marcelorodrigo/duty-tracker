package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.UpdateCompensationRateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCompensationRateUseCase implements UseCase<UpdateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final UpdateCompensationRateValidator validator;

    @Override
    @Transactional
    public CompensationRateResponse execute(UpdateCompensationRateRequest request) {
        validator.validate(request);
        CompensationRate existing = compensationRateGateway
                .findById(request.rateId())
                .orElseThrow(() -> new CompensationRateNotFoundException("Rate not found: " + request.rateId()));
        CompensationRate updated = new CompensationRate(
                existing.id(),
                existing.rateCategory(),
                existing.overtimeDayType(),
                request.label(),
                existing.timeFrom(),
                existing.timeTo(),
                request.percentage());
        CompensationRate saved = compensationRateGateway.update(updated);
        return new CompensationRateResponse(
                saved.id(),
                saved.rateCategory(),
                saved.overtimeDayType(),
                saved.label(),
                saved.timeFrom(),
                saved.timeTo(),
                saved.percentage());
    }
}
