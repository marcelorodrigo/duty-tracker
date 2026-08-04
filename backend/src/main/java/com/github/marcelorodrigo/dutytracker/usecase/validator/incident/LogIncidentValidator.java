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
        if (isNullOrNonPositive(request.onCallPeriodId())) {
            throw new InvalidIncidentException("onCallPeriodId must be a positive number");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

        requireDate(request.startDateTime(), "startDateTime");
        requireDate(request.endDateTime(), "endDateTime");

        var now = LocalDateTime.now(clock);

        if (request.startDateTime().isAfter(now)) {
            throw new InvalidIncidentException("Incident startDateTime cannot be in the future");
        }

        if (request.endDateTime().isAfter(now)) {
            throw new InvalidIncidentException("Incident endDateTime cannot be in the future");
        }

        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime");
        }

        var period = onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidIncidentException("Period not found"));

        if (request.startDateTime().isBefore(period.startDateTime())
                || request.startDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident startDateTime must be within the on-call period");
        }

        if (request.endDateTime().isBefore(period.startDateTime())
                || request.endDateTime().isAfter(period.endDateTime())) {
            throw new InvalidIncidentException("Incident endDateTime must be within the on-call period");
        }

        if (incidentGateway.existsOverlapping(
                request.onCallPeriodId(), request.startDateTime(), request.endDateTime(), null)) {
            throw new IncidentOverlapException();
        }
    }

    private static LocalDateTime requireDate(LocalDateTime value, String field) {
        if (value == null) {
            throw new InvalidIncidentException(field + " is required");
        }
        return value;
    }

    private static boolean isNullOrNonPositive(Long value) {
        return value == null || value <= 0;
    }
}
