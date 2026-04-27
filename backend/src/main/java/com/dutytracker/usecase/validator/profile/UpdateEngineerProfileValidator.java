package com.dutytracker.usecase.validator.profile;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.domain.exceptions.ProfileLockedException;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import org.springframework.stereotype.Component;

@Component
public class UpdateEngineerProfileValidator implements RequestValidator<UpdateEngineerProfileRequest> {

    private final RegistrationSummaryGateway registrationSummaryGateway;

    public UpdateEngineerProfileValidator(RegistrationSummaryGateway registrationSummaryGateway) {
        this.registrationSummaryGateway = registrationSummaryGateway;
    }

    @Override
    public void validate(UpdateEngineerProfileRequest request) {
        if (request.workingDays() == null || request.workingDays().isEmpty()) {
            throw new IllegalArgumentException("At least one working day must be specified");
        }
        if (request.workEndTime() == null || request.workStartTime() == null
                || !request.workEndTime().isAfter(request.workStartTime())) {
            throw new IllegalArgumentException("workEndTime must be after workStartTime");
        }
        if (registrationSummaryGateway.existsAny()) {
            throw new ProfileLockedException("Profile cannot be updated while registration summaries exist");
        }
    }
}
