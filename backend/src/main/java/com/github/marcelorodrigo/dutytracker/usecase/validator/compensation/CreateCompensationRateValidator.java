package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCompensationRateValidator implements RequestValidator<CreateCompensationRateRequest> {

    private final CompensationRateGateway compensationRateGateway;

    @Override
    public void validate(CreateCompensationRateRequest request) {
        if (request.overtimeDayType() == null) {
            throw new IllegalArgumentException("overtimeDayType is required");
        }
        if (request.timeFrom() == null || request.timeTo() == null) {
            throw new IllegalArgumentException("timeFrom and timeTo are required");
        }
        var isDuplicated = compensationRateGateway
                .findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, request.overtimeDayType())
                .stream()
                .anyMatch(r ->
                        r.timeFrom().equals(request.timeFrom()) && r.timeTo().equals(request.timeTo()));
        if (isDuplicated) {
            throw new DuplicateCompensationRateException(
                    "An OVERTIME_ALLOWANCE rate already exists for overtimeDayType=" + request.overtimeDayType()
                            + " timeFrom=" + request.timeFrom()
                            + " timeTo=" + request.timeTo());
        }
    }
}
