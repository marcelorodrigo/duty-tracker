package com.dutytracker.usecase.validator.summary;


import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
