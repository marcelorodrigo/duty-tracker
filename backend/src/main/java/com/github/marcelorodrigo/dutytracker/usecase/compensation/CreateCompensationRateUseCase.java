package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.CreateCompensationRateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCompensationRateUseCase implements UseCase<CreateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final CreateCompensationRateValidator validator;
    private final CompensationRateResponseMapper responseMapper;

    @Override
    public CompensationRateResponse execute(CreateCompensationRateRequest request) {
        validator.validate(request);
        var rate = new CompensationRate(
                null,
                RateCategory.OVERTIME_ALLOWANCE,
                request.overtimeDayType(),
                request.label(),
                request.timeFrom(),
                request.timeTo(),
                request.percentage());
        var saved = compensationRateGateway.save(rate);
        return responseMapper.toResponse(saved);
    }
}
