package com.dutytracker.usecase.compensation;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
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
