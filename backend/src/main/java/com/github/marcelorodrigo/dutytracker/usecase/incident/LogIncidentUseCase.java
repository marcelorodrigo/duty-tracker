package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogIncidentUseCase implements UseCase<LogIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final LogIncidentValidator validator;
    private final IncidentResponseMapper mapper;

    @Override
    @Transactional
    public IncidentResponse execute(final LogIncidentRequest request) {
        validator.validate(request);
        var saved = incidentGateway.save(mapper.toDomain(request));
        return mapper.toResponse(saved);
    }
}
