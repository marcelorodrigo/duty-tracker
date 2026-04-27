package com.dutytracker.application.usecase.incident;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.IncidentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteIncidentUseCase implements UseCase<DeleteIncidentRequest, Void> {

    private final IncidentGateway incidentGateway;
    private final DeleteIncidentValidator validator;

    public DeleteIncidentUseCase(IncidentGateway incidentGateway, DeleteIncidentValidator validator) {
        this.incidentGateway = incidentGateway;
        this.validator = validator;
    }

    @Override
    public Void execute(DeleteIncidentRequest request) {
        validator.validate(request);
        incidentGateway.deleteById(request.incidentId());
        return null;
    }
}
