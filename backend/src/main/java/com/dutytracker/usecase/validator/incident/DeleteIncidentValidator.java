package com.dutytracker.usecase.validator.incident;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.incident.*;
import org.springframework.stereotype.Component;

@Component
public class DeleteIncidentValidator implements RequestValidator<DeleteIncidentRequest> {

    @Override
    public void validate(DeleteIncidentRequest request) {
        // no-op
    }
}
