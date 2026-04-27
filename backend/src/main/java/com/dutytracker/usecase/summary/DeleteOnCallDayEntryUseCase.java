package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import org.springframework.stereotype.Service;

@Service
public class DeleteOnCallDayEntryUseCase implements UseCase<DeleteOnCallDayEntryRequest, Void> {

    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final DeleteOnCallDayEntryValidator validator;

    public DeleteOnCallDayEntryUseCase(OnCallDayEntryGateway onCallDayEntryGateway,
                                        DeleteOnCallDayEntryValidator validator) {
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteOnCallDayEntryRequest request) {
        validator.validate(request);
        onCallDayEntryGateway.deleteById(request.entryId());
        return null;
    }
}
