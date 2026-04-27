package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import org.springframework.stereotype.Service;

@Service
public class DeleteOnCallPeriodUseCase implements UseCase<DeleteOnCallPeriodRequest, Void> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final DeleteOnCallPeriodValidator validator;

    public DeleteOnCallPeriodUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                     DeleteOnCallPeriodValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteOnCallPeriodRequest request) {
        validator.validate(request);
        onCallPeriodGateway.deleteById(request.periodId());
        return null;
    }
}
