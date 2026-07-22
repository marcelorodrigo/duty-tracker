package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogIncidentUseCase implements UseCase<LogIncidentRequest, IncidentResponse> {

    private final IncidentGateway incidentGateway;
    private final LogIncidentValidator validator;
    private final IncidentResponseMapper mapper;
    private final Clock clock;

    @Override
    public IncidentResponse execute(final LogIncidentRequest request) {
        validator.validate(request);
        var createdAt = LocalDateTime.now(clock);
        var saved = incidentGateway.save(mapper.toDomain(request, createdAt));
        return mapper.toResponse(saved);
    }
}
