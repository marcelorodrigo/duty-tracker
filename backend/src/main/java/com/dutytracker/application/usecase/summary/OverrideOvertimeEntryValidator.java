package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import org.springframework.stereotype.Component;

@Component
public class OverrideOvertimeEntryValidator implements RequestValidator<OverrideOvertimeEntryRequest> {

    private final OvertimeEntryGateway overtimeEntryGateway;

    public OverrideOvertimeEntryValidator(OvertimeEntryGateway overtimeEntryGateway) {
        this.overtimeEntryGateway = overtimeEntryGateway;
    }

    @Override
    public void validate(OverrideOvertimeEntryRequest request) {
        overtimeEntryGateway.findById(request.entryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Overtime entry not found"));
    }
}
