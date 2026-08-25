package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.UpdateHolidaysValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateHolidaysUseCase implements UseCase<UpdateHolidaysRequest, List<HolidayResponse>> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final UpdateHolidaysValidator validator;

    @Override
    public List<HolidayResponse> execute(UpdateHolidaysRequest request) {
        validator.validate(request);
        onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new OnCallPeriodNotFoundException(request.periodId()));

        holidayGateway.deleteByOnCallPeriodId(request.periodId());

        List<Holiday> toSave = request.holidays().stream()
                .map(h -> new Holiday(null, request.periodId(), h.date(), h.name()))
                .toList();

        List<Holiday> saved = holidayGateway.saveAll(toSave);

        return saved.stream().map(h -> new HolidayResponse(h.date(), h.name())).toList();
    }
}
