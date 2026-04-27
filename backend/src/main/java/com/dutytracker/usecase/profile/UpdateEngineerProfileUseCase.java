package com.dutytracker.usecase.profile;

import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UpdateEngineerProfileUseCase implements UseCase<UpdateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final UpdateEngineerProfileValidator validator;

    public UpdateEngineerProfileUseCase(
            EngineerProfileGateway profileGateway,
            RegistrationSummaryGateway registrationSummaryGateway,
            UpdateEngineerProfileValidator validator) {
        this.profileGateway = profileGateway;
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.validator = validator;
    }

    @Override
    public EngineerProfileResponse execute(UpdateEngineerProfileRequest request) {
        validator.validate(request);
        EngineerProfile existing = profileGateway
                .find()
                .orElseThrow(() -> new IllegalStateException("No engineer profile found to update"));
        EngineerProfile updated = new EngineerProfile(
                existing.id(),
                request.employeeType(),
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                existing.createdAt());
        EngineerProfile saved = profileGateway.save(updated);
        List<String> days =
                saved.workingDays().stream().map(DayOfWeek::name).sorted().toList();
        return new EngineerProfileResponse(
                saved.id(), saved.employeeType(), days, saved.workStartTime(), saved.workEndTime(), false);
    }
}
