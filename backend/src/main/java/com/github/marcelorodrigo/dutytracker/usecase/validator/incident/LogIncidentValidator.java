package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogIncidentValidator implements RequestValidator<LogIncidentRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public void validate(LogIncidentRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

        if (request.onCallPeriodId() == null) {
            throw new InvalidIncidentException("onCallPeriodId is required");
        }

        var period = onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("Period not found"));

        if (request.startDateTime().isBefore(period.startDateTime())
                || request.startDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident startDateTime must be within the on-call period");
        }
    }
}
