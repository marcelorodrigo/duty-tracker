package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateIncidentValidator implements RequestValidator<UpdateIncidentRequest> {

    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final Clock clock;

    @Override
    public void validate(UpdateIncidentRequest request) {
        var existing = incidentGateway
                .findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));

        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

        var now = LocalDateTime.now(clock);

        if (request.startDateTime().isAfter(now)) {
            throw new InvalidIncidentException("Incident startDateTime cannot be in the future");
        }

        if (request.endDateTime().isAfter(now)) {
            throw new InvalidIncidentException("Incident endDateTime cannot be in the future");
        }

        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime");
        }

        var period = onCallPeriodGateway
                .findById(existing.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("On-call period not found"));

        if (request.startDateTime().isBefore(period.startDateTime())
                || request.startDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident startDateTime must be within the on-call period");
        }

        if (request.endDateTime().isBefore(period.startDateTime())
                || request.endDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be within the on-call period");
        }

        if (incidentGateway.existsOverlapping(
                existing.onCallPeriodId(), request.startDateTime(), request.endDateTime(), request.incidentId())) {
            throw new IncidentOverlapException(
                    "Incident overlaps with an existing incident in the same on-call period");
        }
    }
}
