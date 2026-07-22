package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.CommandUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.DeleteOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.DeleteOnCallPeriodValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOnCallPeriodUseCase implements CommandUseCase<DeleteOnCallPeriodRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final DeleteOnCallPeriodValidator validator;

    @Override
    public void execute(DeleteOnCallPeriodRequest request) {
        validator.validate(request);
        onCallPeriodGateway.deleteById(request.periodId());
    }
}
