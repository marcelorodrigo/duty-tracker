package com.dutytracker.usecase.incident;




import com.dutytracker.domain.Incident;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;
import java.util.List;
import org.springframework.stereotype.Service;
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
