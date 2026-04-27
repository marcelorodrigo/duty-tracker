package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.application.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import com.dutytracker.domain.model.OvertimeEntry;
import org.springframework.stereotype.Service;

@Service
public class AddOvertimeEntryUseCase implements UseCase<AddOvertimeEntryRequest, OvertimeEntryResponse> {

    private final OvertimeEntryGateway overtimeEntryGateway;
    private final AddOvertimeEntryValidator validator;

    public AddOvertimeEntryUseCase(OvertimeEntryGateway overtimeEntryGateway,
                                    AddOvertimeEntryValidator validator) {
        this.overtimeEntryGateway = overtimeEntryGateway;
        this.validator = validator;
    }

    @Override
    public OvertimeEntryResponse execute(AddOvertimeEntryRequest request) {
        validator.validate(request);

        OvertimeEntry saved = overtimeEntryGateway.save(
                new OvertimeEntry(null, request.incidentId(),
                        request.overtimeHours(), request.allowanceHours(), request.allowancePercentage(),
                        request.timeFrom(), request.timeTo(), request.isAllowanceEntry(), true));

        return new OvertimeEntryResponse(
                saved.id(), saved.incidentId(),
                saved.overtimeHours(), saved.allowanceHours(), saved.allowancePercentage(),
                saved.timeFrom(), saved.timeTo(),
                saved.isAllowanceEntry(), saved.manualOverride());
    }
}
