package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateHolidaysUseCase implements UseCase<UpdateHolidaysRequest, List<HolidayResponse>> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;

    @Override
    @Transactional
    public List<HolidayResponse> execute(UpdateHolidaysRequest request) {
        onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));

        holidayGateway.deleteByOnCallPeriodId(request.periodId());

        List<Holiday> toSave = request.holidays().stream()
                .map(h -> new Holiday(null, request.periodId(), h.date(), h.name()))
                .toList();

        List<Holiday> saved = holidayGateway.saveAll(toSave);

        return saved.stream().map(h -> new HolidayResponse(h.date(), h.name())).toList();
    }
}
