package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.GetIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.GetIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetIncidentUseCase implements UseCase<GetIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final GetIncidentValidator validator;

    @Override
    public IncidentResponse execute(GetIncidentRequest request) {
        validator.validate(request);
        var incident =
                incidentGateway.findById(request.id()).orElseThrow(() -> new IncidentNotFoundException(request.id()));
        return new IncidentResponse(
                incident.id(),
                incident.onCallPeriodId(),
                incident.name(),
                incident.startDateTime(),
                incident.endDateTime(),
                incident.createdAt());
    }
}
