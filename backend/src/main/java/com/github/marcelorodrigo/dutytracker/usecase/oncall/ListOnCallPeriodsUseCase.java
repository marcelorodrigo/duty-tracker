package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.ListOnCallPeriodsValidator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListOnCallPeriodsUseCase implements UseCase<ListOnCallPeriodsRequest, OnCallPeriodListResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final ListOnCallPeriodsValidator validator;

    @Override
    public OnCallPeriodListResponse execute(ListOnCallPeriodsRequest request) {
        validator.validate(request);
        Page<OnCallPeriod> page = onCallPeriodGateway.findAll(request.pagination());
        List<OnCallPeriod> periods = page.getContent();

        if (periods.isEmpty()) {
            return new OnCallPeriodListResponse(
                    List.of(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }

        // Batch fetch holidays for the periods on the current page in a single query
        List<Long> periodIds = periods.stream().map(OnCallPeriod::id).toList();
        Map<Long, List<Holiday>> holidaysByPeriodId = holidayGateway.findByOnCallPeriodIds(periodIds);

        // Build responses with the batched holidays
        List<OnCallPeriodResponse> responses = periods.stream()
                .map(period -> toResponse(period, holidaysByPeriodId.getOrDefault(period.id(), List.of())))
                .toList();
        return new OnCallPeriodListResponse(
                responses, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<Holiday> holidays) {
        List<HolidayResponse> holidayResponses = holidays.stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();
        return new OnCallPeriodResponse(
                period.id(), period.startDateTime(), period.endDateTime(), holidayResponses, period.createdAt());
    }
}
