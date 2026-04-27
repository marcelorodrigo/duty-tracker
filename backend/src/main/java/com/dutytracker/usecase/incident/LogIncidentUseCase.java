package com.dutytracker.usecase.incident;

import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;
import java.time.Instant;
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
                null, request.onCallPeriodId(), request.date(), request.startTime(), request.endTime(), Instant.now()));
        return new IncidentResponse(
                saved.id(),
                saved.onCallPeriodId(),
                saved.date(),
                saved.startTime(),
                saved.endTime(),
                saved.createdAt());
    }
}
