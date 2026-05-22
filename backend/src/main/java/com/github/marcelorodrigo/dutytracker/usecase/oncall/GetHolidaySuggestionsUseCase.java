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

        return publicHolidayGateway.getHolidaysWithNames(request.start(), request.end()).stream()
                .map(h -> new HolidayResponse(h.date(), h.name()))
                .toList();
    }
}
