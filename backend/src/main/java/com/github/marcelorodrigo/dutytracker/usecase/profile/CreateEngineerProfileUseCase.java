package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.CreateEngineerProfileValidator;
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
    private final EngineerProfileDefaults defaults;

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        var hourlyRate = request.hourlyRate() != null ? request.hourlyRate() : defaults.hourlyRate();
        var standbyWeekdaySaturdayPercentage = request.standbyWeekdaySaturdayPercentage() != null
                ? request.standbyWeekdaySaturdayPercentage()
                : defaults.standbyWeekdaySaturdayPercentage();
        var standbyWeekdaySundayHolidayPercentage = request.standbyWeekdaySundayHolidayPercentage() != null
                ? request.standbyWeekdaySundayHolidayPercentage()
                : defaults.standbyWeekdaySundayHolidayPercentage();
        EngineerProfile profile = new EngineerProfile(
                null,
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                hourlyRate,
                standbyWeekdaySaturdayPercentage,
                standbyWeekdaySundayHolidayPercentage,
                null);
        EngineerProfile saved = profileGateway.save(profile);
        List<String> days = saved.workingDays().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(DayOfWeek::name)
                .toList();
        return new EngineerProfileResponse(
                saved.id(),
                days,
                saved.workStartTime(),
                saved.workEndTime(),
                saved.hourlyRate(),
                saved.standbyWeekdaySaturdayPercentage(),
                saved.standbyWeekdaySundayHolidayPercentage());
    }
}
