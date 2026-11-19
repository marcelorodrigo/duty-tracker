package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.CreateEngineerProfileValidator;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateEngineerProfileUseCase implements UseCase<CreateEngineerProfileRequest, EngineerProfileResponse> {

    private static final BigDecimal DEFAULT_HOURLY_RATE = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_STANDBY_WEEKDAY_SATURDAY_PERCENTAGE = new BigDecimal("0.067");
    private static final BigDecimal DEFAULT_STANDBY_WEEKDAY_SUNDAY_HOLIDAY_PERCENTAGE = new BigDecimal("0.084");

    private final EngineerProfileGateway profileGateway;
    private final CreateEngineerProfileValidator validator;

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        BigDecimal hourlyRateToUse = request.hourlyRate() != null ? request.hourlyRate() : DEFAULT_HOURLY_RATE;
        BigDecimal weekdaySat = request.standbyWeekdaySaturdayPercentage() != null
                ? request.standbyWeekdaySaturdayPercentage()
                : DEFAULT_STANDBY_WEEKDAY_SATURDAY_PERCENTAGE;
        BigDecimal sundayHol = request.standbyWeekdaySundayHolidayPercentage() != null
                ? request.standbyWeekdaySundayHolidayPercentage()
                : DEFAULT_STANDBY_WEEKDAY_SUNDAY_HOLIDAY_PERCENTAGE;
        String calendarFeedUrlToUse = resolveCalendarFeedUrl(request.calendarFeedUrl());
        EngineerProfile profile = new EngineerProfile(
                null,
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                hourlyRateToUse,
                weekdaySat,
                sundayHol,
                calendarFeedUrlToUse,
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
                saved.standbyWeekdaySundayHolidayPercentage(),
                saved.calendarFeedUrl());
    }

    private String resolveCalendarFeedUrl(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
