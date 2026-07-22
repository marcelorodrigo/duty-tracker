package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogIncidentValidator implements RequestValidator<LogIncidentRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final IncidentGateway incidentGateway;
    private final Clock clock;

    @Override
    public void validate(LogIncidentRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

        var startDateTime = requireDate(request.startDateTime(), "startDateTime");
        var endDateTime = requireDate(request.endDateTime(), "endDateTime");

        var now = LocalDateTime.now(clock);

        if (startDateTime.isAfter(now)) {
            throw new InvalidIncidentException("Incident startDateTime cannot be in the future");
        }

        if (endDateTime.isAfter(now)) {
            throw new InvalidIncidentException("Incident endDateTime cannot be in the future");
        }

        if (!endDateTime.isAfter(startDateTime)) {
            throw new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime");
        }

        var period = onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("Period not found"));

        if (startDateTime.isBefore(period.startDateTime()) || startDateTime.isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident startDateTime must be within the on-call period");
        }

        if (endDateTime.isBefore(period.startDateTime()) || endDateTime.isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be within the on-call period");
        }

        if (incidentGateway.existsOverlapping(request.onCallPeriodId(), startDateTime, endDateTime, null)) {
            throw new IncidentOverlapException();
        }
    }

    private static LocalDateTime requireDate(LocalDateTime value, String field) {
        if (value == null) {
            throw new InvalidIncidentException(field + " is required");
        }
        return value;
    }
}
