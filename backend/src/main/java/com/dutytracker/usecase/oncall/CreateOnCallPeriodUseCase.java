package com.dutytracker.usecase.oncall;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.UserPreferencesGateway;
import com.dutytracker.domain.HolidayOverride;
import com.dutytracker.domain.OnCallPeriod;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class CreateOnCallPeriodUseCase implements UseCase<CreateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final UserPreferencesGateway userPreferencesGateway;
    private final CreateOnCallPeriodValidator validator;

    public CreateOnCallPeriodUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                     HolidayOverrideGateway holidayOverrideGateway,
                                     UserPreferencesGateway userPreferencesGateway,
                                     CreateOnCallPeriodValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.userPreferencesGateway = userPreferencesGateway;
        this.validator = validator;
    }

    @Override
    public OnCallPeriodResponse execute(CreateOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = new OnCallPeriod(null, request.startDateTime(), request.endDateTime(), Instant.now());
        OnCallPeriod saved = onCallPeriodGateway.save(period);
        return toResponse(saved, List.of());
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<HolidayOverride> overrides) {
        return new OnCallPeriodResponse(
                period.id(), period.startDateTime(), period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt()
        );
    }
}
