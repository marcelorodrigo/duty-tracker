package com.dutytracker.usecase.oncall;

import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import org.springframework.stereotype.Service;

@Service
public class OverrideOnCallDayEntryUseCase implements UseCase<OverrideOnCallDayEntryRequest, OnCallDayEntryResponse> {

    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final OverrideOnCallDayEntryValidator validator;

    public OverrideOnCallDayEntryUseCase(OnCallDayEntryGateway onCallDayEntryGateway,
                                          OverrideOnCallDayEntryValidator validator) {
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.validator = validator;
    }

    @Override
    public OnCallDayEntryResponse execute(OverrideOnCallDayEntryRequest request) {
        validator.validate(request);

        OnCallDayEntry existing = onCallDayEntryGateway.findById(request.entryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Day entry not found"));

        OnCallDayEntry updated = new OnCallDayEntry(
                existing.id(),
                existing.onCallPeriodId(),
                existing.date(),
                request.hours() != null ? request.hours() : existing.hours(),
                request.rateType() != null ? request.rateType() : existing.rateType(),
                existing.capped(),
                request.timeForTimeFlag() != null ? request.timeForTimeFlag() : existing.timeForTimeFlag(),
                true
        );

        OnCallDayEntry saved = onCallDayEntryGateway.save(updated);

        return new OnCallDayEntryResponse(
                saved.id(),
                saved.date(),
                saved.hours(),
                saved.rateType(),
                saved.capped(),
                saved.timeForTimeFlag(),
                saved.manualOverride()
        );
    }
}
