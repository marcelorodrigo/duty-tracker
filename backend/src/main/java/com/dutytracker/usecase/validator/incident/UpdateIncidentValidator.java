package com.dutytracker.usecase.validator.incident;

import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.validator.RequestValidator;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class UpdateIncidentValidator implements RequestValidator<UpdateIncidentRequest> {

    private final IncidentGateway incidentGateway;

    public UpdateIncidentValidator(IncidentGateway incidentGateway) {
        this.incidentGateway = incidentGateway;
    }

    @Override
    public void validate(UpdateIncidentRequest request) {
        incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));

        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidIncidentException("Incident date cannot be in the future");
        }
    }
}
