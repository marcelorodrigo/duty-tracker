package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.GetEngineerProfileValidator;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEngineerProfileUseCase implements UseCase<GetEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final GetEngineerProfileValidator validator;

    @Override
    public EngineerProfileResponse execute(GetEngineerProfileRequest request) {
        validator.validate(request);
        return profileGateway
                .find()
                .map(profile -> {
                    List<String> days = profile.workingDays().stream()
                            .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                            .map(DayOfWeek::name)
                            .toList();
                    return new EngineerProfileResponse(
                            profile.id(),
                            days,
                            profile.workStartTime(),
                            profile.workEndTime(),
                            profile.hourlyRate(),
                            profile.standbyWeekdaySaturdayPercentage(),
                            profile.standbyWeekdaySundayHolidayPercentage());
                })
                .orElseThrow(ProfileNotFoundException::new);
    }
}
