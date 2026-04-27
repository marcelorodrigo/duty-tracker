package com.dutytracker.usecase.summary;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import org.springframework.stereotype.Service;

@Service
public class OverrideOvertimeEntryUseCase implements UseCase<OverrideOvertimeEntryRequest, OvertimeEntryResponse> {

    private final OvertimeEntryGateway overtimeEntryGateway;
    private final OverrideOvertimeEntryValidator validator;

    public OverrideOvertimeEntryUseCase(
            OvertimeEntryGateway overtimeEntryGateway, OverrideOvertimeEntryValidator validator) {
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.validator = validator;
    }

    @Override
    public OvertimeEntryResponse execute(OverrideOvertimeEntryRequest request) {
        validator.validate(request);

        OvertimeEntry existing = overtimeEntryGateway
                .findById(request.entryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Overtime entry not found"));

        OvertimeEntry updated = new OvertimeEntry(
                existing.id(),
                existing.incidentId(),
                request.overtimeHours() != null ? request.overtimeHours() : existing.overtimeHours(),
                request.allowanceHours() != null ? request.allowanceHours() : existing.allowanceHours(),
                request.allowancePercentage() != null ? request.allowancePercentage() : existing.allowancePercentage(),
                existing.timeFrom(),
                existing.timeTo(),
                existing.isAllowanceEntry(),
                true);

        OvertimeEntry saved = overtimeEntryGateway.save(updated);

        return new OvertimeEntryResponse(
                saved.id(),
                saved.incidentId(),
                saved.overtimeHours(),
                saved.allowanceHours(),
                saved.allowancePercentage(),
                saved.timeFrom(),
                saved.timeTo(),
                saved.isAllowanceEntry(),
                saved.manualOverride());
    }
}
