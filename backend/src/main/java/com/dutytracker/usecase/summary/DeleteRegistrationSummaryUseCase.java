package com.dutytracker.usecase.summary;

import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteRegistrationSummaryUseCase implements UseCase<DeleteRegistrationSummaryRequest, Void> {

    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final DeleteRegistrationSummaryValidator validator;

    @Override
    public Void execute(DeleteRegistrationSummaryRequest request) {
        validator.validate(request);
        registrationSummaryGateway.deleteById(request.summaryId());
        return null;
    }
}
