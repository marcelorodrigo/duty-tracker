package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GetOnCallPeriodHolidaysValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOnCallPeriodHolidaysUseCase implements UseCase<GetOnCallPeriodHolidaysRequest, List<HolidayResponse>> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final GetOnCallPeriodHolidaysValidator validator;

    @Override
    public List<HolidayResponse> execute(GetOnCallPeriodHolidaysRequest request) {
        validator.validate(request);
        onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new OnCallPeriodNotFoundException(request.periodId()));
        return holidayGateway.findByOnCallPeriodId(request.periodId()).stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();
    }
}
