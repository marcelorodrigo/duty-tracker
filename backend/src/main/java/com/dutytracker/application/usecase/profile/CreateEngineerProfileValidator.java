package com.dutytracker.application.usecase.profile;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.ProfileAlreadyExistsException;
import com.dutytracker.domain.gateway.EngineerProfileGateway;
import org.springframework.stereotype.Component;

@Component
public class CreateEngineerProfileValidator implements RequestValidator<CreateEngineerProfileRequest> {

    private final EngineerProfileGateway profileGateway;

    public CreateEngineerProfileValidator(EngineerProfileGateway profileGateway) {
        this.profileGateway = profileGateway;
    }

    @Override
    public void validate(CreateEngineerProfileRequest request) {
        if (request.workingDays() == null || request.workingDays().isEmpty()) {
            throw new IllegalArgumentException("At least one working day must be specified");
        }
        if (request.workEndTime() == null || request.workStartTime() == null
                || !request.workEndTime().isAfter(request.workStartTime())) {
            throw new IllegalArgumentException("workEndTime must be after workStartTime");
        }
        if (profileGateway.find().isPresent()) {
            throw new ProfileAlreadyExistsException("An engineer profile already exists");
        }
    }
}
