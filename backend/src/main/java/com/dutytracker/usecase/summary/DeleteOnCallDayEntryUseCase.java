package com.dutytracker.usecase.summary;

import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOnCallDayEntryUseCase implements UseCase<DeleteOnCallDayEntryRequest, Void> {

    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final DeleteOnCallDayEntryValidator validator;

    @Override
    public Void execute(DeleteOnCallDayEntryRequest request) {
        validator.validate(request);
        onCallDayEntryGateway.deleteById(request.entryId());
        return null;
    }
}
