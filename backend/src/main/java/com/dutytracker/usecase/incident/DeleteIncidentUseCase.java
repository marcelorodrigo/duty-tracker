package com.dutytracker.usecase.incident;

import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteIncidentUseCase implements UseCase<DeleteIncidentRequest, Void> {

    private final IncidentGateway incidentGateway;
    private final DeleteIncidentValidator validator;

    @Override
    public Void execute(DeleteIncidentRequest request) {
        validator.validate(request);
        incidentGateway.deleteById(request.incidentId());
        return null;
    }
}
