package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import com.github.marcelorodrigo.dutytracker.gateway.api.HolidaysApi;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetHolidaySuggestionsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HolidayController implements HolidaysApi {

    private final GetHolidaySuggestionsUseCase getSuggestions;

    @Override
    public ResponseEntity<List<HolidayResponse>> getHolidaySuggestions(LocalDate start, LocalDate end) {
        return ResponseEntity.ok(getSuggestions.execute(new GetHolidaySuggestionsRequest(start, end)));
    }
}
