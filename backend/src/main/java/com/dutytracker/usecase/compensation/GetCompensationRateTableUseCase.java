package com.dutytracker.usecase.compensation;

import com.dutytracker.domain.CompensationRate;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetCompensationRateTableUseCase
        implements UseCase<GetCompensationRateTableRequest, CompensationRateTableResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final GetCompensationRateTableValidator validator;

    public GetCompensationRateTableUseCase(
            CompensationRateGateway compensationRateGateway, GetCompensationRateTableValidator validator) {
        this.compensationRateGateway = compensationRateGateway;
        this.validator = validator;
    }

    @Override
    public CompensationRateTableResponse execute(GetCompensationRateTableRequest request) {
        validator.validate(request);
        List<CompensationRate> rates = request.employeeType() == null
                ? compensationRateGateway.findAll()
                : compensationRateGateway.findByEmployeeType(request.employeeType());
        List<CompensationRateResponse> responses = rates.stream()
                .map(r -> new CompensationRateResponse(
                        r.id(),
                        r.employeeType(),
                        r.rateCategory(),
                        r.label(),
                        r.timeFrom(),
                        r.timeTo(),
                        r.percentage()))
                .toList();
        return new CompensationRateTableResponse(responses);
    }
}
