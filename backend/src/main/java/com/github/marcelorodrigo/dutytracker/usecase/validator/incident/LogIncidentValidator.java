package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.LocalDate;
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

        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidIncidentException("Incident date cannot be in the future");
        }

        if (request.onCallPeriodId() != null) {
            var period = onCallPeriodGateway
                    .findById(request.onCallPeriodId())
                    .orElseThrow(() -> new InvalidIncidentException("Period not found"));

            LocalDate periodStart = period.startDateTime().toLocalDate();
            LocalDate periodEnd = period.endDateTime().toLocalDate();

            if (request.date().isBefore(periodStart) || request.date().isAfter(periodEnd)) {
                throw new InvalidIncidentException("Date not within on-call period");
            }
        }
    }
}
