package com.dutytracker.application.usecase.incident;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.IncidentGateway;
import com.dutytracker.domain.model.Incident;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListIncidentsUseCase implements UseCase<ListIncidentsRequest, IncidentListResponse> {

    private final IncidentGateway incidentGateway;
    private final ListIncidentsValidator validator;

    public ListIncidentsUseCase(IncidentGateway incidentGateway, ListIncidentsValidator validator) {
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public IncidentListResponse execute(ListIncidentsRequest request) {
        validator.validate(request);
        List<Incident> incidents = request.onCallPeriodId() != null
                ? incidentGateway.findByOnCallPeriodId(request.onCallPeriodId())
                : incidentGateway.findAll();
        List<IncidentResponse> responses = incidents.stream()
                .map(i -> new IncidentResponse(i.id(), i.onCallPeriodId(), i.date(), i.startTime(), i.endTime(), i.createdAt()))
                .toList();
        return new IncidentListResponse(responses);
    }
}
