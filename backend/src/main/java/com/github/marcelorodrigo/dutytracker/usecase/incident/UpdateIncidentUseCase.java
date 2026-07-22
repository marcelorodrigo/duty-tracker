package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.UpdateIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateIncidentUseCase implements UseCase<UpdateIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final UpdateIncidentValidator validator;

    @Override
    @Transactional
    public IncidentResponse execute(UpdateIncidentRequest request) {
        Incident existing = incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));
        validator.validate(request);
        OnCallPeriod period = onCallPeriodGateway
                .findById(existing.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("On-call period not found"));
        validateWithinPeriod(request, period);
        if (incidentGateway.existsOverlapping(
                existing.onCallPeriodId(), request.startDateTime(), request.endDateTime(), request.incidentId())) {
            throw new IncidentOverlapException();
        }
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

    private void validateWithinPeriod(UpdateIncidentRequest request, OnCallPeriod period) {
        if (request.startDateTime().isBefore(period.startDateTime())
                || request.startDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident startDateTime must be within the on-call period");
        }
        if (request.endDateTime().isBefore(period.startDateTime())
                || request.endDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be within the on-call period");
        }
    }
}
