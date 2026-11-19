package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.PageResult;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.ListOnCallPeriodsValidator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListOnCallPeriodsUseCase implements UseCase<ListOnCallPeriodsRequest, OnCallPeriodListResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final ListOnCallPeriodsValidator validator;
    private final HolidayResponseMapper holidayResponseMapper;

    @Override
    public OnCallPeriodListResponse execute(ListOnCallPeriodsRequest request) {
        validator.validate(request);
        PageResult<OnCallPeriod> page = onCallPeriodGateway.findAll(request.pagination());
        List<OnCallPeriod> periods = page.content();

        if (periods.isEmpty()) {
            return new OnCallPeriodListResponse(
                    List.of(), page.page(), page.size(), page.totalElements(), page.totalPages());
        }

        // Batch fetch holidays for the periods on the current page in a single query
        List<Long> periodIds = periods.stream().map(OnCallPeriod::id).toList();
        Map<Long, List<Holiday>> holidaysByPeriodId = holidayGateway.findByOnCallPeriodIds(periodIds);

        // Build responses with the batched holidays
        List<OnCallPeriodResponse> responses = periods.stream()
                .map(period -> toResponse(period, holidaysByPeriodId.getOrDefault(period.id(), List.of())))
                .toList();
        return new OnCallPeriodListResponse(
                responses, page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<Holiday> holidays) {
        List<HolidayResponse> holidayResponses =
                holidays.stream().map(holidayResponseMapper::toResponse).toList();
        return new OnCallPeriodResponse(
                period.id(), period.startDateTime(), period.endDateTime(), holidayResponses, period.createdAt());
    }
}
