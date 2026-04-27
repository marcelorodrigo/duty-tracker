package com.dutytracker.usecase.compensation;

import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateCompensationRateUseCase implements UseCase<CreateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final CreateCompensationRateValidator validator;

    public CreateCompensationRateUseCase(CompensationRateGateway compensationRateGateway,
                                         CreateCompensationRateValidator validator) {
        this.compensationRateGateway = compensationRateGateway;
        this.validator = validator;
    }

    @Override
    public CompensationRateResponse execute(CreateCompensationRateRequest request) {
        validator.validate(request);
        CompensationRate rate = new CompensationRate(
                null,
                request.employeeType(),
                RateCategory.OVERTIME_ALLOWANCE,
                request.label(),
                request.timeFrom(),
                request.timeTo(),
                request.percentage()
        );
        List<CompensationRate> saved = compensationRateGateway.saveAll(List.of(rate));
        CompensationRate result = saved.getFirst();
        return new CompensationRateResponse(
                result.id(), result.employeeType(), result.rateCategory(),
                result.label(), result.timeFrom(), result.timeTo(), result.percentage());
    }
}
