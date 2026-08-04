package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import org.springframework.stereotype.Component;

@Component
public class CalculateOvertimeEntriesValidator {
    public void validate(CalculateOvertimeEntriesRequest request) {
        if (request.incidentId() == null || request.incidentId() <= 0) {
            throw new InvalidIncidentException("Incident id must be a positive number");
        }
    }
}
