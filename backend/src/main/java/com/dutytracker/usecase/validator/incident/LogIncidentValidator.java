package com.dutytracker.usecase.validator.incident;

import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.validator.RequestValidator;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogIncidentValidator implements RequestValidator<LogIncidentRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public void validate(LogIncidentRequest request) {
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
