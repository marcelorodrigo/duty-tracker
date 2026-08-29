package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.ListIncidentsValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListIncidentsUseCase implements UseCase<ListIncidentsRequest, IncidentListResponse> {

    private final IncidentGateway incidentGateway;
    private final ListIncidentsValidator validator;

    @Override
    public IncidentListResponse execute(ListIncidentsRequest request) {
        validator.validate(request);
        Page<Incident> page = request.onCallPeriodId() != null
                ? incidentGateway.findByOnCallPeriodId(request.onCallPeriodId(), request.pageable())
                : incidentGateway.findAll(request.pageable());
        List<IncidentResponse> responses = page.getContent().stream()
                .map(i -> new IncidentResponse(
                        i.id(), i.onCallPeriodId(), i.name(), i.startDateTime(), i.endDateTime(), i.createdAt()))
                .toList();
        return new IncidentListResponse(
                responses, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
