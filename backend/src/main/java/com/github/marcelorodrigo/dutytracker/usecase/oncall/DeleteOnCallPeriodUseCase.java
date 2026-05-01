package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.DeleteOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.DeleteOnCallPeriodValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOnCallPeriodUseCase implements UseCase<DeleteOnCallPeriodRequest, Void> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final DeleteOnCallPeriodValidator validator;

    @Override
    public Void execute(DeleteOnCallPeriodRequest request) {
        validator.validate(request);
        onCallPeriodGateway.deleteById(request.periodId());
        return null;
    }
}
