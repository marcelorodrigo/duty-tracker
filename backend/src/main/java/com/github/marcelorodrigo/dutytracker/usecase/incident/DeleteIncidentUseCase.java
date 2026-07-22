package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.CommandUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.DeleteIncidentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteIncidentUseCase implements CommandUseCase<DeleteIncidentRequest> {

    private final IncidentGateway incidentGateway;
    private final DeleteIncidentValidator validator;

    @Override
    public void execute(DeleteIncidentRequest request) {
        validator.validate(request);
        incidentGateway.deleteById(request.incidentId());
    }
}
