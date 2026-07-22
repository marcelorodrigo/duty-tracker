package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListIncidentsUseCase implements UseCase<ListIncidentsRequest, IncidentListResponse> {

    private final IncidentGateway incidentGateway;

    @Override
    @Transactional(readOnly = true)
    public IncidentListResponse execute(ListIncidentsRequest request) {
        List<Incident> incidents = request.onCallPeriodId() != null
                ? incidentGateway.findByOnCallPeriodId(request.onCallPeriodId())
                : incidentGateway.findAll();
        List<IncidentResponse> responses = incidents.stream()
                .map(i -> new IncidentResponse(
                        i.id(), i.onCallPeriodId(), i.name(), i.startDateTime(), i.endDateTime(), i.createdAt()))
                .toList();
        return new IncidentListResponse(responses);
    }
}
