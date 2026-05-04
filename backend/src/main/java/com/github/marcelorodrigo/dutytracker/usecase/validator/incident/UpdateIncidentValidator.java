package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateIncidentValidator implements RequestValidator<UpdateIncidentRequest> {

    private final IncidentGateway incidentGateway;

    @Override
    public void validate(UpdateIncidentRequest request) {
        incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));

        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

        if (request.startDateTime().toLocalDate().isAfter(LocalDate.now())) {
            throw new InvalidIncidentException("Incident date cannot be in the future");
        }
    }
}
