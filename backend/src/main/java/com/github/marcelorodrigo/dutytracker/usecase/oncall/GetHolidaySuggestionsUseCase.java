package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GetHolidaySuggestionsValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetHolidaySuggestionsUseCase implements UseCase<GetHolidaySuggestionsRequest, List<HolidayResponse>> {

    private final PublicHolidayGateway publicHolidayGateway;
    private final GetHolidaySuggestionsValidator validator;

    @Override
    public List<HolidayResponse> execute(GetHolidaySuggestionsRequest request) {
        validator.validate(request);

        var start = request.start();
        var end = request.end();

        return publicHolidayGateway.getHolidaysWithNames(start.getYear()).stream()
                .filter(h -> !h.date().isBefore(start) && !h.date().isAfter(end))
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();
    }
}
