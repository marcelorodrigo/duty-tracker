package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.UpdateEngineerProfileValidator;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEngineerProfileUseCase implements UseCase<UpdateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final UpdateEngineerProfileValidator validator;

    @Override
    @Transactional
    public EngineerProfileResponse execute(UpdateEngineerProfileRequest request) {
        validator.validate(request);
        EngineerProfile existing = profileGateway
                .find()
                .orElseThrow(() -> new IllegalStateException("No engineer profile found to update"));
        Money hourlyRateToUse = request.hourlyRate() != null ? Money.of(request.hourlyRate()) : existing.hourlyRate();
        Percentage weekdaySat = request.standbyWeekdaySaturdayPercentage() != null
                ? Percentage.of(request.standbyWeekdaySaturdayPercentage())
                : existing.standbyWeekdaySaturdayPercentage();
        Percentage sundayHol = request.standbyWeekdaySundayHolidayPercentage() != null
                ? Percentage.of(request.standbyWeekdaySundayHolidayPercentage())
                : existing.standbyWeekdaySundayHolidayPercentage();
        EngineerProfile updated = existing.withSettings(
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                hourlyRateToUse,
                weekdaySat,
                sundayHol);
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
                saved.hourlyRate().value(),
                saved.standbyWeekdaySaturdayPercentage().value(),
                saved.standbyWeekdaySundayHolidayPercentage().value());
    }
}
