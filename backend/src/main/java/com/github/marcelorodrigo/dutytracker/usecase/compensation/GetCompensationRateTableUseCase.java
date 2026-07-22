package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateTableResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.GetCompensationRateTableValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCompensationRateTableUseCase
        implements UseCase<GetCompensationRateTableRequest, CompensationRateTableResponse> {

    private final CompensationRateGateway compensationRateGateway;
    private final GetCompensationRateTableValidator validator;

    @Override
    @Transactional(readOnly = true)
    public CompensationRateTableResponse execute(GetCompensationRateTableRequest request) {
        validator.validate(request);
        List<CompensationRate> rates = compensationRateGateway.findAll();
        List<CompensationRateResponse> responses = rates.stream()
                .map(r -> new CompensationRateResponse(
                        r.id(),
                        r.rateCategory(),
                        r.overtimeDayType(),
                        r.label(),
                        r.timeFrom(),
                        r.timeTo(),
                        r.percentage()))
                .toList();
        return new CompensationRateTableResponse(responses);
    }
}
