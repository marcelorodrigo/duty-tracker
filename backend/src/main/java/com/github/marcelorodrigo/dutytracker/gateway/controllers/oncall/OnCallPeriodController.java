package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/oncall-periods")
@Tag(name = "On-Call Periods", description = "Manage on-call periods, scheduling, and day entries")
@RequiredArgsConstructor
public class OnCallPeriodController {

    private final CreateOnCallPeriodUseCase createPeriod;
    private final GetOnCallPeriodUseCase getPeriod;
    private final ListOnCallPeriodsUseCase listPeriods;
    private final UpdateOnCallPeriodUseCase updatePeriod;
    private final DeleteOnCallPeriodUseCase deletePeriod;
    private final GetOnCallPeriodHolidaysUseCase getHolidays;
    private final UpdateHolidaysUseCase updateHolidays;
    private final CalculateOnCallDayEntriesUseCase calculateEntries;
    private final GenerateOnCallPeriodReportUseCase generateReport;

    @PostMapping
    @Operation(summary = "Create on-call period", description = "Create a new on-call period with start and end times")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "On-call period created successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallPeriodResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid period data")
            })
    public ResponseEntity<OnCallPeriodResponse> create(@RequestBody CreateOnCallPeriodRequest request) {
        var response = createPeriod.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/oncall-periods/" + response.id()))
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List on-call periods", description = "Retrieve all on-call periods")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "On-call periods retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallPeriodListResponse.class)))
            })
    public ResponseEntity<OnCallPeriodListResponse> list() {
        return ResponseEntity.ok(listPeriods.execute(new ListOnCallPeriodsRequest()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get on-call period", description = "Retrieve details of a specific on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "On-call period retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallPeriodResponse.class))),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<OnCallPeriodResponse> get(
            @Parameter(description = "On-call period ID") @PathVariable Long id) {
        return ResponseEntity.ok(getPeriod.execute(new GetOnCallPeriodRequest(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update on-call period", description = "Update the start and end times of an on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "On-call period updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallPeriodResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid period data"),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<OnCallPeriodResponse> update(
            @Parameter(description = "On-call period ID") @PathVariable Long id,
            @RequestBody UpdateOnCallPeriodRequest request) {
        var req = new UpdateOnCallPeriodRequest(id, request.startDateTime(), request.endDateTime());
        return ResponseEntity.ok(updatePeriod.execute(req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete on-call period", description = "Remove an on-call period from the system")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "On-call period deleted successfully"),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<Void> delete(@Parameter(description = "On-call period ID") @PathVariable Long id) {
        deletePeriod.execute(new DeleteOnCallPeriodRequest(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/holidays")
    @Operation(
            summary = "Get holidays for on-call period",
            description = "Retrieve all holidays registered for a specific on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<List<HolidayResponse>> getHolidays(
            @Parameter(description = "On-call period ID") @PathVariable Long id) {
        return ResponseEntity.ok(getHolidays.execute(new GetOnCallPeriodHolidaysRequest(id)));
    }

    @PutMapping("/{id}/holidays")
    @Operation(
            summary = "Update holidays for on-call period",
            description = "Bulk-replace the holidays for an on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Holidays updated successfully"),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<List<HolidayResponse>> updateHolidays(
            @Parameter(description = "On-call period ID") @PathVariable Long id,
            @RequestBody List<HolidayResponse> body) {
        return ResponseEntity.ok(updateHolidays.execute(new UpdateHolidaysRequest(id, body)));
    }

    @PostMapping("/{id}/calculate")
    @Operation(
            summary = "Calculate on-call day entries",
            description = "Generate day entries for the entire on-call period")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Day entries calculated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallDayEntriesResponse.class))),
                @ApiResponse(responseCode = "404", description = "On-call period not found")
            })
    public ResponseEntity<OnCallDayEntriesResponse> calculate(
            @Parameter(description = "On-call period ID") @PathVariable Long id) {
        return ResponseEntity.ok(calculateEntries.execute(new CalculateOnCallDayEntriesRequest(id)));
    }

    @GetMapping("/{id}/report")
    @Operation(
            summary = "Generate on-call period report",
            description =
                    "Generate a full report for the on-call period including standby lines and overtime lines for MyHR")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Report generated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = OnCallPeriodReportResponse.class))),
                @ApiResponse(responseCode = "404", description = "On-call period not found"),
                @ApiResponse(
                        responseCode = "409",
                        description = "One or more incidents occur entirely during working hours")
            })
    public ResponseEntity<OnCallPeriodReportResponse> report(
            @Parameter(description = "On-call period ID") @PathVariable Long id) {
        return ResponseEntity.ok(generateReport.execute(new GenerateOnCallPeriodReportRequest(id)));
    }
}
