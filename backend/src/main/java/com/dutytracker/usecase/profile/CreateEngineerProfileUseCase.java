package com.dutytracker.usecase.profile;

import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateEngineerProfileUseCase implements UseCase<CreateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final CreateEngineerProfileValidator validator;

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        EngineerProfile profile = new EngineerProfile(
                null,
                request.employeeType(),
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                null);
        EngineerProfile saved = profileGateway.save(profile);
        List<String> days = saved.workingDays().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(DayOfWeek::name)
                .toList();
        return new EngineerProfileResponse(
                saved.id(), saved.employeeType(), days, saved.workStartTime(), saved.workEndTime());
    }
}
