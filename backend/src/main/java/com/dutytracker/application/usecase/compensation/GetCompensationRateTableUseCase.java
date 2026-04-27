package com.dutytracker.application.usecase.compensation;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.CompensationRateGateway;
import com.dutytracker.domain.model.CompensationRate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCompensationRateTableUseCase implements UseCase<GetCompensationRateTableRequest, CompensationRateTableResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final GetCompensationRateTableValidator validator;

    public GetCompensationRateTableUseCase(CompensationRateGateway compensationRateGateway,
                                           GetCompensationRateTableValidator validator) {
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
                        r.id(), r.employeeType(), r.rateCategory(),
                        r.label(), r.timeFrom(), r.timeTo(), r.percentage()))
                .toList();
        return new CompensationRateTableResponse(responses);
    }
}
