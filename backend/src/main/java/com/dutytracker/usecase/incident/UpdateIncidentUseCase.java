package com.dutytracker.usecase.incident;

import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.domain.Incident;
import com.dutytracker.domain.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateIncidentUseCase implements UseCase<UpdateIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final UpdateIncidentValidator validator;

    public UpdateIncidentUseCase(IncidentGateway incidentGateway, UpdateIncidentValidator validator) {
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public IncidentResponse execute(UpdateIncidentRequest request) {
        validator.validate(request);
        Incident existing = incidentGateway.findById(request.incidentId())
                .orElseThrow(() -> new InvalidIncidentException("Incident not found"));
        Incident updated = incidentGateway.save(new Incident(
                existing.id(),
                existing.onCallPeriodId(),
                request.date(),
                request.startTime(),
                request.endTime(),
                existing.createdAt()
        ));
        return new IncidentResponse(
                updated.id(),
                updated.onCallPeriodId(),
                updated.date(),
                updated.startTime(),
                updated.endTime(),
                updated.createdAt()
        );
    }
}
