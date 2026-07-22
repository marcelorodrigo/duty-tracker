package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateOnCallDayEntriesValidator;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalculateOnCallDayEntriesUseCase
        implements UseCase<CalculateOnCallDayEntriesRequest, OnCallDayEntriesResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final OnCallDayEntriesCalculator calculator;
    private final CalculateOnCallDayEntriesValidator validator;

    @Override
    @Transactional(readOnly = true)
    public OnCallDayEntriesResponse execute(CalculateOnCallDayEntriesRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        EngineerProfile profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));

        Set<LocalDate> holidayDates = holidayGateway.findByOnCallPeriodId(periodId).stream()
                .map(Holiday::date)
                .collect(Collectors.toSet());

        return calculator.calculate(period, profile, holidayDates);
    }
}
