package com.dutytracker.usecase.oncall;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateOnCallPeriodUseCase implements UseCase<CreateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final CreateOnCallPeriodValidator validator;

    @Override
    public OnCallPeriodResponse execute(CreateOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = new OnCallPeriod(null, request.startDateTime(), request.endDateTime(), Instant.now());
        OnCallPeriod saved = onCallPeriodGateway.save(period);
        return toResponse(saved, List.of());
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<HolidayOverride> overrides) {
        return new OnCallPeriodResponse(
                period.id(),
                period.startDateTime(),
                period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt());
    }
}
