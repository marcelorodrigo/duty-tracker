package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.model.OnCallDayEntry;
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
