package com.dutytracker.application.usecase.incident;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteIncidentValidator implements RequestValidator<DeleteIncidentRequest> {

    @Override
    public void validate(DeleteIncidentRequest request) {
        // no-op
    }
}
