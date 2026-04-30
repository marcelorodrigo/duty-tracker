package com.dutytracker.usecase.validator.profile;

import com.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
        if (profileGateway.find().isPresent()) {
            throw new ProfileAlreadyExistsException("An engineer profile already exists");
        }
    }
}
