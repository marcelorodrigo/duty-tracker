package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOnCallPeriodUseCase implements UseCase<CreateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final CreateOnCallPeriodValidator validator;
    private final Clock clock;

    @Override
    @Transactional
    public OnCallPeriodResponse execute(CreateOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period =
                new OnCallPeriod(null, request.startDateTime(), request.endDateTime(), LocalDateTime.now(clock));
        OnCallPeriod saved = onCallPeriodGateway.save(period);
        return new OnCallPeriodResponse(
                saved.id(), saved.startDateTime(), saved.endDateTime(), List.of(), saved.createdAt());
    }
}
