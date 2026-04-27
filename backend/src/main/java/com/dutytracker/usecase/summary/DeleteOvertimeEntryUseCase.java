package com.dutytracker.usecase.summary;

import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOvertimeEntryUseCase implements UseCase<DeleteOvertimeEntryRequest, Void> {

    private final OvertimeEntryGateway overtimeEntryGateway;
    private final DeleteOvertimeEntryValidator validator;

    @Override
    public Void execute(DeleteOvertimeEntryRequest request) {
        validator.validate(request);
        overtimeEntryGateway.deleteById(request.entryId());
        return null;
    }
}
