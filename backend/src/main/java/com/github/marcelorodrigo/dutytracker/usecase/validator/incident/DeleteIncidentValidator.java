package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteIncidentValidator implements RequestValidator<DeleteIncidentRequest> {

    private final IncidentGateway incidentGateway;

    @Override
    public void validate(DeleteIncidentRequest request) {
        if (request.incidentId() == null || request.incidentId() <= 0) {
            throw new InvalidIncidentException("Incident id must be a positive number");
        }

        incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new IncidentNotFoundException(request.incidentId()));
    }
}
