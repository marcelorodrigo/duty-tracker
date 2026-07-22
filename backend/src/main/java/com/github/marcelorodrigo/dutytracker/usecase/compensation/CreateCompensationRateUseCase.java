package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.CreateCompensationRateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCompensationRateUseCase implements UseCase<CreateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final CreateCompensationRateValidator validator;
    private final CompensationRateResponseMapper responseMapper;

    @Override
    @Transactional
    public CompensationRateResponse execute(CreateCompensationRateRequest request) {
        validator.validate(request);
        var isDuplicated = compensationRateGateway
                .findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, request.overtimeDayType())
                .stream()
                .anyMatch(rate -> rate.timeFrom().equals(request.timeFrom())
                        && rate.timeTo().equals(request.timeTo()));
        if (isDuplicated) {
            throw new DuplicateCompensationRateException(
                    "An OVERTIME_ALLOWANCE rate already exists for overtimeDayType=" + request.overtimeDayType()
                            + " timeFrom=" + request.timeFrom()
                            + " timeTo=" + request.timeTo());
        }
        var rate = CompensationRate.overtimeAllowance(
                request.overtimeDayType(), request.label(), request.timeFrom(), request.timeTo(), request.percentage());
        var saved = compensationRateGateway.save(rate);
        return responseMapper.toResponse(saved);
    }
}
