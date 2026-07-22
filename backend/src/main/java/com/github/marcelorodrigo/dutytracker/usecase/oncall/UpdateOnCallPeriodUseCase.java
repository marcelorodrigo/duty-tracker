package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.UpdateOnCallPeriodValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateOnCallPeriodUseCase implements UseCase<UpdateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final UpdateOnCallPeriodValidator validator;

    @Override
    @Transactional
    public OnCallPeriodResponse execute(UpdateOnCallPeriodRequest request) {
        validator.validate(request);
        if (onCallPeriodGateway.existsOverlapping(request.startDateTime(), request.endDateTime(), request.periodId())) {
            throw new OnCallPeriodOverlapException();
        }
        OnCallPeriod existing = onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
        OnCallPeriod updated = existing.reschedule(request.startDateTime(), request.endDateTime());
        OnCallPeriod saved = onCallPeriodGateway.save(updated);

        holidayGateway.deleteOutOfRange(
                saved.id(),
                saved.startDateTime().toLocalDate(),
                saved.endDateTime().toLocalDate());

        List<Holiday> holidays = holidayGateway.findByOnCallPeriodId(saved.id());
        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();

        return new OnCallPeriodResponse(
                saved.id(), saved.startDateTime(), saved.endDateTime(), holidayResponses, saved.createdAt());
    }
}
