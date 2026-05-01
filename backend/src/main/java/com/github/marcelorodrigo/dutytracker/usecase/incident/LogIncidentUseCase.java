package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogIncidentUseCase implements UseCase<LogIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final LogIncidentValidator validator;

    @Override
    public IncidentResponse execute(LogIncidentRequest request) {
        validator.validate(request);
        Incident saved = incidentGateway.save(new Incident(
                null,
                request.onCallPeriodId(),
                request.name(),
                request.date(),
                request.startTime(),
                request.endTime(),
                LocalDateTime.now()));
        return new IncidentResponse(
                saved.id(),
                saved.onCallPeriodId(),
                saved.name(),
                saved.date(),
                saved.startTime(),
                saved.endTime(),
                saved.createdAt());
    }
}
