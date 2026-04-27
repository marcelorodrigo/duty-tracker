package com.dutytracker.gateway.controllers.summary;

import com.dutytracker.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.usecase.oncall.OverrideOnCallDayEntryRequest;
import com.dutytracker.usecase.oncall.OverrideOnCallDayEntryUseCase;
import com.dutytracker.usecase.summary.*;
import com.dutytracker.domain.StandbyRateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/summaries")
@Tag(name = "Registration Summaries", description = "Manage registration summaries and overtime/on-call entries")
public class RegistrationSummaryController {

    private final CreateRegistrationSummaryUseCase createSummary;
    private final GetRegistrationSummaryUseCase getSummary;
    private final ListRegistrationSummariesUseCase listSummaries;
    private final DeleteRegistrationSummaryUseCase deleteSummary;
    private final OverrideOnCallDayEntryUseCase overrideOnCallEntry;
    private final DeleteOnCallDayEntryUseCase deleteOnCallEntry;
    private final OverrideOvertimeEntryUseCase overrideOvertimeEntry;
    private final DeleteOvertimeEntryUseCase deleteOvertimeEntry;
    private final AddOnCallDayEntryUseCase addOnCallEntry;
    private final AddOvertimeEntryUseCase addOvertimeEntry;

    public RegistrationSummaryController(CreateRegistrationSummaryUseCase createSummary,
                                          GetRegistrationSummaryUseCase getSummary,
                                          ListRegistrationSummariesUseCase listSummaries,
                                          DeleteRegistrationSummaryUseCase deleteSummary,
                                          OverrideOnCallDayEntryUseCase overrideOnCallEntry,
                                          DeleteOnCallDayEntryUseCase deleteOnCallEntry,
                                          OverrideOvertimeEntryUseCase overrideOvertimeEntry,
                                          DeleteOvertimeEntryUseCase deleteOvertimeEntry,
                                          AddOnCallDayEntryUseCase addOnCallEntry,
                                          AddOvertimeEntryUseCase addOvertimeEntry) {
        this.createSummary = createSummary;
        this.getSummary = getSummary;
        this.listSummaries = listSummaries;
        this.deleteSummary = deleteSummary;
        this.overrideOnCallEntry = overrideOnCallEntry;
        this.deleteOnCallEntry = deleteOnCallEntry;
        this.overrideOvertimeEntry = overrideOvertimeEntry;
        this.deleteOvertimeEntry = deleteOvertimeEntry;
        this.addOnCallEntry = addOnCallEntry;
        this.addOvertimeEntry = addOvertimeEntry;
    }

    // ── Summary CRUD ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create registration summary", description = "Create a new registration summary for an on-call period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration summary created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid summary data")
    })
    public ResponseEntity<RegistrationSummaryResponse> create(@RequestBody CreateSummaryBody body) {
        var response = createSummary.execute(new CreateRegistrationSummaryRequest(body.periodId(), body.label()));
        return ResponseEntity.created(URI.create("/api/v1/summaries/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "List registration summaries", description = "Retrieve all registration summaries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration summaries retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationSummaryListResponse.class)))
    })
    public ResponseEntity<RegistrationSummaryListResponse> list() {
        return ResponseEntity.ok(listSummaries.execute(new ListRegistrationSummariesRequest()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get registration summary", description = "Retrieve details of a specific registration summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration summary retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Registration summary not found")
    })
    public ResponseEntity<RegistrationSummaryResponse> get(@Parameter(description = "Registration summary ID") @PathVariable Long id) {
        return ResponseEntity.ok(getSummary.execute(new GetRegistrationSummaryRequest(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete registration summary", description = "Remove a registration summary from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registration summary deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Registration summary not found")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Registration summary ID") @PathVariable Long id) {
        deleteSummary.execute(new DeleteRegistrationSummaryRequest(id));
        return ResponseEntity.noContent().build();
    }

    // ── On-Call Day Entries ───────────────────────────────────────────────────

    @PostMapping("/{id}/oncall-entries")
    @Operation(summary = "Add on-call day entry", description = "Create a new on-call day entry within a registration summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "On-call day entry added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnCallDayEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid entry data")
    })
    public ResponseEntity<OnCallDayEntryResponse> addOnCallEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @RequestBody AddOnCallEntryBody body) {
        var response = addOnCallEntry.execute(
                new AddOnCallDayEntryRequest(id, body.date(), body.hours(), body.rateType()));
        return ResponseEntity.created(
                URI.create("/api/v1/summaries/" + id + "/oncall-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}/oncall-entries/{entryId}")
    @Operation(summary = "Override on-call day entry", description = "Modify hours, rate type, or time-for-time flag for an on-call entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "On-call entry overridden successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnCallDayEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid override data"),
            @ApiResponse(responseCode = "404", description = "Summary or entry not found")
    })
    public ResponseEntity<OnCallDayEntryResponse> overrideOnCallEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @Parameter(description = "On-call day entry ID") @PathVariable Long entryId,
            @RequestBody OnCallEntryOverrideBody body) {
        return ResponseEntity.ok(overrideOnCallEntry.execute(
                new OverrideOnCallDayEntryRequest(entryId, body.hours(), body.rateType(), body.timeForTimeFlag())));
    }

    @DeleteMapping("/{id}/oncall-entries/{entryId}")
    @Operation(summary = "Delete on-call day entry", description = "Remove an on-call day entry from a registration summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "On-call entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Summary or entry not found")
    })
    public ResponseEntity<Void> deleteOnCallEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @Parameter(description = "On-call day entry ID") @PathVariable Long entryId) {
        deleteOnCallEntry.execute(new DeleteOnCallDayEntryRequest(entryId));
        return ResponseEntity.noContent().build();
    }

    // ── Overtime Entries ──────────────────────────────────────────────────────

    @PostMapping("/{id}/overtime-entries")
    @Operation(summary = "Add overtime entry", description = "Create a new overtime entry within a registration summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Overtime entry added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OvertimeEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid entry data")
    })
    public ResponseEntity<OvertimeEntryResponse> addOvertimeEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @RequestBody AddOvertimeEntryBody body) {
        var response = addOvertimeEntry.execute(
                new AddOvertimeEntryRequest(body.incidentId(), body.overtimeHours(),
                        body.allowanceHours(), body.allowancePercentage(),
                        body.timeFrom(), body.timeTo(), body.isAllowanceEntry()));
        return ResponseEntity.created(
                URI.create("/api/v1/summaries/" + id + "/overtime-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}/overtime-entries/{entryId}")
    @Operation(summary = "Override overtime entry", description = "Modify hours and allowance details for an overtime entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overtime entry overridden successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OvertimeEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid override data"),
            @ApiResponse(responseCode = "404", description = "Summary or entry not found")
    })
    public ResponseEntity<OvertimeEntryResponse> overrideOvertimeEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @Parameter(description = "Overtime entry ID") @PathVariable Long entryId,
            @RequestBody OvertimeEntryOverrideBody body) {
        return ResponseEntity.ok(overrideOvertimeEntry.execute(
                new OverrideOvertimeEntryRequest(entryId, body.overtimeHours(),
                        body.allowanceHours(), body.allowancePercentage())));
    }

    @DeleteMapping("/{id}/overtime-entries/{entryId}")
    @Operation(summary = "Delete overtime entry", description = "Remove an overtime entry from a registration summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Overtime entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Summary or entry not found")
    })
    public ResponseEntity<Void> deleteOvertimeEntry(
            @Parameter(description = "Registration summary ID") @PathVariable Long id,
            @Parameter(description = "Overtime entry ID") @PathVariable Long entryId) {
        deleteOvertimeEntry.execute(new DeleteOvertimeEntryRequest(entryId));
        return ResponseEntity.noContent().build();
    }

    // ── Inner request body records ────────────────────────────────────────────

    record CreateSummaryBody(Long periodId, String label) {}

    record AddOnCallEntryBody(LocalDate date, BigDecimal hours, StandbyRateType rateType) {}

    record OnCallEntryOverrideBody(BigDecimal hours, StandbyRateType rateType, Boolean timeForTimeFlag) {}

    record AddOvertimeEntryBody(Long incidentId, BigDecimal overtimeHours, BigDecimal allowanceHours,
                                BigDecimal allowancePercentage, LocalTime timeFrom, LocalTime timeTo,
                                boolean isAllowanceEntry) {}

    record OvertimeEntryOverrideBody(BigDecimal overtimeHours, BigDecimal allowanceHours,
                                     BigDecimal allowancePercentage) {}
}
