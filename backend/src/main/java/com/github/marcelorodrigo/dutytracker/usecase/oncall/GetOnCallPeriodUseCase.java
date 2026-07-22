package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GetOnCallPeriodValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOnCallPeriodUseCase implements UseCase<GetOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final GetOnCallPeriodValidator validator;

    @Override
    @Transactional(readOnly = true)
    public OnCallPeriodResponse execute(GetOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
        List<Holiday> holidays = holidayGateway.findByOnCallPeriodId(period.id());
        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();
        return new OnCallPeriodResponse(
                period.id(), period.startDateTime(), period.endDateTime(), holidayResponses, period.createdAt());
    }
}
