package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.ListIncidentsValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListIncidentsUseCase implements UseCase<ListIncidentsRequest, IncidentListResponse> {

    private final IncidentGateway incidentGateway;
    private final ListIncidentsValidator validator;
    private final IncidentResponseMapper responseMapper;

    @Override
    public IncidentListResponse execute(ListIncidentsRequest request) {
        validator.validate(request);
        List<Incident> incidents = request.onCallPeriodId() != null
                ? incidentGateway.findByOnCallPeriodId(request.onCallPeriodId())
                : incidentGateway.findAll();
        var responses = incidents.stream().map(responseMapper::toResponse).toList();
        return new IncidentListResponse(responses);
    }
}
