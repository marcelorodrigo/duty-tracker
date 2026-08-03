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

@Service
@RequiredArgsConstructor
public class UpdateCompensationRateUseCase implements UseCase<UpdateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final UpdateCompensationRateValidator validator;
    private final CompensationRateResponseMapper responseMapper;

    @Override
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
        return responseMapper.toResponse(saved);
    }
}
