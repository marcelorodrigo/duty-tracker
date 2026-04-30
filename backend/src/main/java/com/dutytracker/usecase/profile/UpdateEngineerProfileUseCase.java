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
public class UpdateEngineerProfileUseCase implements UseCase<UpdateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final UpdateEngineerProfileValidator validator;

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
        List<String> days = saved.workingDays().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(DayOfWeek::name)
                .toList();
        return new EngineerProfileResponse(
                saved.id(), saved.employeeType(), days, saved.workStartTime(), saved.workEndTime());
    }
}
