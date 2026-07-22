package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateIncidentValidator implements RequestValidator<UpdateIncidentRequest> {

    private final Clock clock;

    @Override
    public void validate(UpdateIncidentRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidIncidentException("name is required");
        }

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
    }
}
