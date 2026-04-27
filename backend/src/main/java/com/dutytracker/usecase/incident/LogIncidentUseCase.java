package com.dutytracker.usecase.incident;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.domain.Incident;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class LogIncidentUseCase implements UseCase<LogIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final LogIncidentValidator validator;

    public LogIncidentUseCase(IncidentGateway incidentGateway, LogIncidentValidator validator) {
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public IncidentResponse execute(LogIncidentRequest request) {
        validator.validate(request);
        Incident saved = incidentGateway.save(new Incident(
                null,
                request.onCallPeriodId(),
                request.date(),
                request.startTime(),
                request.endTime(),
                Instant.now()
        ));
        return new IncidentResponse(
                saved.id(),
                saved.onCallPeriodId(),
                saved.date(),
                saved.startTime(),
                saved.endTime(),
                saved.createdAt()
        );
    }
}
