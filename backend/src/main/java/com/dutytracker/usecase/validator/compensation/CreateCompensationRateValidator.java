package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.domain.RateCategory;
import com.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
        var isDuplicated = compensationRateGateway.findAll().stream()
                .filter(r -> r.rateCategory() == RateCategory.OVERTIME_ALLOWANCE)
                .anyMatch(r -> r.employeeType() == request.employeeType()
                        && r.overtimeDayType() == request.overtimeDayType()
                        && r.timeFrom().equals(request.timeFrom())
                        && r.timeTo().equals(request.timeTo()));
        if (isDuplicated) {
            throw new DuplicateCompensationRateException(
                    "An OVERTIME_ALLOWANCE rate already exists for employeeType=" + request.employeeType()
                            + " overtimeDayType=" + request.overtimeDayType()
                            + " timeFrom=" + request.timeFrom()
                            + " timeTo=" + request.timeTo());
        }
    }
}
