package com.dutytracker.usecase.validator.summary;

import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverrideOvertimeEntryValidator implements RequestValidator<OverrideOvertimeEntryRequest> {

    private final OvertimeEntryGateway overtimeEntryGateway;

    @Override
    public void validate(OverrideOvertimeEntryRequest request) {
        overtimeEntryGateway
                .findById(request.entryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Overtime entry not found"));
    }
}
