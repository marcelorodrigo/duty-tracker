package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateEngineerProfileValidator implements RequestValidator<CreateEngineerProfileRequest> {

    private final EngineerProfileGateway profileGateway;

    @Override
    public void validate(CreateEngineerProfileRequest request) {
        if (request.workingDays() == null || request.workingDays().isEmpty()) {
            throw new InvalidEngineerProfileException("At least one working day must be specified");
        }
        if (request.workEndTime() == null
                || request.workStartTime() == null
                || !request.workEndTime().isAfter(request.workStartTime())) {
            throw new InvalidEngineerProfileException("workEndTime must be after workStartTime");
        }
        if (request.hourlyRate() != null && request.hourlyRate().compareTo(BigDecimal.ONE) <= 0) {
            throw new InvalidHourlyRateException("Hourly rate must be greater than 1");
        }
        if (profileGateway.find().isPresent()) {
            throw new ProfileAlreadyExistsException("An engineer profile already exists");
        }
    }
}
