package com.dutytracker.usecase.validator.incident;

import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteIncidentValidator implements RequestValidator<DeleteIncidentRequest> {

    @Override
    public void validate(DeleteIncidentRequest request) {
        // no-op
    }
}
