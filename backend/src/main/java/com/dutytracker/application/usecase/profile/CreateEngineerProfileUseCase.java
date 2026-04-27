package com.dutytracker.application.usecase.profile;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.EngineerProfileGateway;
import com.dutytracker.domain.model.EngineerProfile;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class CreateEngineerProfileUseCase implements UseCase<CreateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final CreateEngineerProfileValidator validator;

    public CreateEngineerProfileUseCase(EngineerProfileGateway profileGateway,
                                        CreateEngineerProfileValidator validator) {
        this.profileGateway = profileGateway;
        this.validator = validator;
    }

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        EngineerProfile profile = new EngineerProfile(
                null,
                request.employeeType(),
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                null
        );
        EngineerProfile saved = profileGateway.save(profile);
        List<String> days = saved.workingDays().stream().map(DayOfWeek::name).sorted().toList();
        return new EngineerProfileResponse(
                saved.id(),
                saved.employeeType(),
                days,
                saved.workStartTime(),
                saved.workEndTime(),
                false
        );
    }
}
