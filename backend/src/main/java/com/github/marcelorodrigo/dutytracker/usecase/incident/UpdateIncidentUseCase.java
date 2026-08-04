package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.UpdateIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateIncidentUseCase implements UseCase<UpdateIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final UpdateIncidentValidator validator;

    @Override
    public IncidentResponse execute(UpdateIncidentRequest request) {
        validator.validate(request);
        Incident existing = incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new IncidentNotFoundException(request.incidentId()));
        Incident updated = incidentGateway.save(new Incident(
                existing.id(),
                existing.onCallPeriodId(),
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                existing.createdAt()));
        return new IncidentResponse(
                updated.id(),
                updated.onCallPeriodId(),
                updated.name(),
                updated.startDateTime(),
                updated.endDateTime(),
                updated.createdAt());
    }
}
