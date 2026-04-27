package com.dutytracker.usecase.validator.profile;

import com.dutytracker.domain.exceptions.ProfileLockedException;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateEngineerProfileValidator implements RequestValidator<UpdateEngineerProfileRequest> {

    private final RegistrationSummaryGateway registrationSummaryGateway;

    @Override
    public void validate(UpdateEngineerProfileRequest request) {
        if (request.workingDays() == null || request.workingDays().isEmpty()) {
            throw new IllegalArgumentException("At least one working day must be specified");
        }
        if (request.workEndTime() == null
                || request.workStartTime() == null
                || !request.workEndTime().isAfter(request.workStartTime())) {
            throw new IllegalArgumentException("workEndTime must be after workStartTime");
        }
        if (registrationSummaryGateway.existsAny()) {
            throw new ProfileLockedException();
        }
    }
}
