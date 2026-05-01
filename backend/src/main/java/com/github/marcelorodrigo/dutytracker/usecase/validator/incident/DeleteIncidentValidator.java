package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteIncidentValidator implements RequestValidator<DeleteIncidentRequest> {

    @Override
    public void validate(DeleteIncidentRequest request) {
        // no-op
    }
}
