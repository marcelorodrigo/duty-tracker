package com.dutytracker.application.usecase.profile;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.EngineerProfileGateway;
import com.dutytracker.domain.gateway.RegistrationSummaryGateway;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class GetEngineerProfileUseCase implements UseCase<GetEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final GetEngineerProfileValidator validator;

    public GetEngineerProfileUseCase(EngineerProfileGateway profileGateway,
                                     RegistrationSummaryGateway registrationSummaryGateway,
                                     GetEngineerProfileValidator validator) {
        this.profileGateway = profileGateway;
        this.registrationSummaryGateway = registrationSummaryGateway;
        this.validator = validator;
    }

    @Override
    public EngineerProfileResponse execute(GetEngineerProfileRequest request) {
        validator.validate(request);
        boolean locked = registrationSummaryGateway.existsAny();
        return profileGateway.find().map(profile -> {
            List<String> days = profile.workingDays().stream().map(DayOfWeek::name).sorted().toList();
            return new EngineerProfileResponse(
                    profile.id(),
                    profile.employeeType(),
                    days,
                    profile.workStartTime(),
                    profile.workEndTime(),
                    locked
            );
        }).orElse(null);
    }
}
