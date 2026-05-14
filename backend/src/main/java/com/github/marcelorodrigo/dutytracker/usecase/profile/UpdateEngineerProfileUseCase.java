package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.UpdateEngineerProfileValidator;
import java.math.BigDecimal;
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
        BigDecimal hourlyRateToUse = request.hourlyRate() != null ? request.hourlyRate() : existing.hourlyRate();
        BigDecimal weekdaySat = request.standbyWeekdaySaturdayPercentage() != null
                ? request.standbyWeekdaySaturdayPercentage()
                : existing.standbyWeekdaySaturdayPercentage();
        BigDecimal sundayHol = request.standbyWeekdaySundayHolidayPercentage() != null
                ? request.standbyWeekdaySundayHolidayPercentage()
                : existing.standbyWeekdaySundayHolidayPercentage();
        EngineerProfile updated = new EngineerProfile(
                existing.id(),
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                hourlyRateToUse,
                weekdaySat,
                sundayHol,
                existing.createdAt());
        EngineerProfile saved = profileGateway.save(updated);
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
