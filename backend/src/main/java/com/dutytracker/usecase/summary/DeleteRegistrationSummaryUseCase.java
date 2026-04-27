package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import org.springframework.stereotype.Service;

@Service
public class DeleteRegistrationSummaryUseCase implements UseCase<DeleteRegistrationSummaryRequest, Void> {

    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final DeleteRegistrationSummaryValidator validator;

    public DeleteRegistrationSummaryUseCase(RegistrationSummaryGateway registrationSummaryGateway,
                                             DeleteRegistrationSummaryValidator validator) {
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteRegistrationSummaryRequest request) {
        validator.validate(request);
        registrationSummaryGateway.deleteById(request.summaryId());
        return null;
    }
}
