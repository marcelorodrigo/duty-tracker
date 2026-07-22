package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.CreateEngineerProfileValidator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateEngineerProfileUseCase implements UseCase<CreateEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final CreateEngineerProfileValidator validator;
    private final EngineerProfileResponseMapper responseMapper;

    @Override
    public EngineerProfileResponse execute(CreateEngineerProfileRequest request) {
        validator.validate(request);
        BigDecimal hourlyRateToUse = request.hourlyRate() != null ? request.hourlyRate() : BigDecimal.ONE;
        BigDecimal weekdaySat = request.standbyWeekdaySaturdayPercentage() != null
                ? request.standbyWeekdaySaturdayPercentage()
                : new BigDecimal("0.067");
        BigDecimal sundayHol = request.standbyWeekdaySundayHolidayPercentage() != null
                ? request.standbyWeekdaySundayHolidayPercentage()
                : new BigDecimal("0.084");
        EngineerProfile profile = new EngineerProfile(
                null,
                request.workingDays(),
                request.workStartTime(),
                request.workEndTime(),
                hourlyRateToUse,
                weekdaySat,
                sundayHol,
                null);
        EngineerProfile saved = profileGateway.save(profile);
        return responseMapper.toResponse(saved);
    }
}
