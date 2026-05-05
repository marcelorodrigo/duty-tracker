package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetHolidaySuggestionsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/holidays")
@Tag(name = "Holidays", description = "Holiday suggestions from public holiday calendar")
@RequiredArgsConstructor
public class HolidayController {

    private final GetHolidaySuggestionsUseCase getSuggestions;

    @GetMapping("/suggestions")
    @Operation(
            summary = "Get holiday suggestions",
            description = "Query the public holiday calendar for Netherlands holidays within a date range")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid date range")
            })
    public ResponseEntity<List<HolidayResponse>> suggestions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(getSuggestions.execute(new GetHolidaySuggestionsRequest(start, end)));
    }
}
