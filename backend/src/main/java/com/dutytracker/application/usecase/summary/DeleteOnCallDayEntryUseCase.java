package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
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
