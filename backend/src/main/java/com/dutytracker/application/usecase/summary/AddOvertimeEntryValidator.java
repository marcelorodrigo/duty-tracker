package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidIncidentException;
import com.dutytracker.domain.gateway.IncidentGateway;
import org.springframework.stereotype.Component;

@Component
public class AddOvertimeEntryValidator implements RequestValidator<AddOvertimeEntryRequest> {

    private final IncidentGateway incidentGateway;

    public AddOvertimeEntryValidator(IncidentGateway incidentGateway) {
        this.incidentGateway = incidentGateway;
    }

    @Override
    public void validate(AddOvertimeEntryRequest request) {
        incidentGateway.findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));
    }
}
