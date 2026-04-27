package com.dutytracker.usecase.profile;

import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEngineerProfileUseCase implements UseCase<GetEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final RegistrationSummaryGateway registrationSummaryGateway;
    private final GetEngineerProfileValidator validator;

    @Override
    public EngineerProfileResponse execute(GetEngineerProfileRequest request) {
        validator.validate(request);
        boolean locked = registrationSummaryGateway.existsAny();
        return profileGateway
                .find()
                .map(profile -> {
                    List<String> days = profile.workingDays().stream()
                            .map(DayOfWeek::name)
                            .sorted()
                            .toList();
                    return new EngineerProfileResponse(
                            profile.id(),
                            profile.employeeType(),
                            days,
                            profile.workStartTime(),
                            profile.workEndTime(),
                            locked);
                })
                .orElse(null);
    }
}
