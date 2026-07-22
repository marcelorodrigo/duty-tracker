package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogIncidentUseCase implements UseCase<LogIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final OnCallPeriodGateway onCallPeriodGateway;
    private final LogIncidentValidator validator;
    private final IncidentResponseMapper mapper;

    @Override
    @Transactional
    public IncidentResponse execute(final LogIncidentRequest request) {
        validator.validate(request);
        OnCallPeriod period = onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("Period not found"));
        validateWithinPeriod(request, period);
        if (incidentGateway.existsOverlapping(
                request.onCallPeriodId(), request.startDateTime(), request.endDateTime(), null)) {
            throw new IncidentOverlapException();
        }
        var saved = incidentGateway.save(mapper.toDomain(request));
        return mapper.toResponse(saved);
    }

    private void validateWithinPeriod(LogIncidentRequest request, OnCallPeriod period) {
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
