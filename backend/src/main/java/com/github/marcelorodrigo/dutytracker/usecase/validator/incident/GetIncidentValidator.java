package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.GetIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetIncidentValidator implements RequestValidator<GetIncidentRequest> {

    @Override
    public void validate(GetIncidentRequest request) {
        if (request.id() == null || request.id() <= 0) {
            throw new InvalidIncidentException("Incident id must be a positive number");
        }
    }
}
