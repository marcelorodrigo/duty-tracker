package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import org.springframework.stereotype.Service;

@Service
public class DeleteOvertimeEntryUseCase implements UseCase<DeleteOvertimeEntryRequest, Void> {

    private final OvertimeEntryGateway overtimeEntryGateway;
    private final DeleteOvertimeEntryValidator validator;

    public DeleteOvertimeEntryUseCase(OvertimeEntryGateway overtimeEntryGateway,
                                       DeleteOvertimeEntryValidator validator) {
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteOvertimeEntryRequest request) {
        validator.validate(request);
        overtimeEntryGateway.deleteById(request.entryId());
        return null;
    }
}
