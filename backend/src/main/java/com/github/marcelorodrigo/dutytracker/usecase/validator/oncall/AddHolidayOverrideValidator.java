package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.AddHolidayOverrideRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddHolidayOverrideValidator implements RequestValidator<AddHolidayOverrideRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;

    @Override
    public void validate(AddHolidayOverrideRequest request) {
        OnCallPeriod period = onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));

        var startDate = period.startDateTime().toLocalDate();
        var endDate = period.endDateTime().toLocalDate();
        if (request.date().isBefore(startDate) || request.date().isAfter(endDate)) {
            throw new InvalidOnCallPeriodException("Date not within period");
        }

        boolean duplicate = holidayOverrideGateway.findByOnCallPeriodId(request.periodId()).stream()
                .anyMatch(o -> o.date().equals(request.date()));
        if (duplicate) {
            throw new HolidayAlreadyRegisteredException();
        }
    }
}
