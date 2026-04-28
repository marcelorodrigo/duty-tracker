package com.dutytracker.usecase.compensation;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCompensationRateUseCase implements UseCase<CreateCompensationRateRequest, CompensationRateResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final CreateCompensationRateValidator validator;

    @Override
    public CompensationRateResponse execute(CreateCompensationRateRequest request) {
        validator.validate(request);
        CompensationRate rate = new CompensationRate(
                null,
                request.employeeType(),
                RateCategory.OVERTIME_ALLOWANCE,
                request.overtimeDayType(),
                request.label(),
                request.timeFrom(),
                request.timeTo(),
                request.percentage());
        List<CompensationRate> saved = compensationRateGateway.saveAll(List.of(rate));
        CompensationRate result = saved.getFirst();
        return new CompensationRateResponse(
                result.id(),
                result.employeeType(),
                result.rateCategory(),
                result.overtimeDayType(),
                result.label(),
                result.timeFrom(),
                result.timeTo(),
                result.percentage());
    }
}
