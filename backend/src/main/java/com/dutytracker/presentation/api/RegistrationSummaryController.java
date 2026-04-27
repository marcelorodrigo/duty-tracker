package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.application.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.application.usecase.oncall.OverrideOnCallDayEntryRequest;
import com.dutytracker.application.usecase.oncall.OverrideOnCallDayEntryUseCase;
import com.dutytracker.application.usecase.summary.*;
import com.dutytracker.domain.model.StandbyRateType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/summaries")
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
    public ResponseEntity<RegistrationSummaryResponse> create(@RequestBody CreateSummaryBody body) {
        var response = createSummary.execute(new CreateRegistrationSummaryRequest(body.periodId(), body.label()));
        return ResponseEntity.created(URI.create("/api/v1/summaries/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<RegistrationSummaryListResponse> list() {
        return ResponseEntity.ok(listSummaries.execute(new ListRegistrationSummariesRequest()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationSummaryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(getSummary.execute(new GetRegistrationSummaryRequest(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteSummary.execute(new DeleteRegistrationSummaryRequest(id));
        return ResponseEntity.noContent().build();
    }

    // ── On-Call Day Entries ───────────────────────────────────────────────────

    @PostMapping("/{id}/oncall-entries")
    public ResponseEntity<OnCallDayEntryResponse> addOnCallEntry(@PathVariable Long id,
                                                                  @RequestBody AddOnCallEntryBody body) {
        var response = addOnCallEntry.execute(
                new AddOnCallDayEntryRequest(id, body.date(), body.hours(), body.rateType()));
        return ResponseEntity.created(
                URI.create("/api/v1/summaries/" + id + "/oncall-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}/oncall-entries/{entryId}")
    public ResponseEntity<OnCallDayEntryResponse> overrideOnCallEntry(@PathVariable Long id,
                                                                       @PathVariable Long entryId,
                                                                       @RequestBody OnCallEntryOverrideBody body) {
        return ResponseEntity.ok(overrideOnCallEntry.execute(
                new OverrideOnCallDayEntryRequest(entryId, body.hours(), body.rateType(), body.timeForTimeFlag())));
    }

    @DeleteMapping("/{id}/oncall-entries/{entryId}")
    public ResponseEntity<Void> deleteOnCallEntry(@PathVariable Long id,
                                                   @PathVariable Long entryId) {
        deleteOnCallEntry.execute(new DeleteOnCallDayEntryRequest(entryId));
        return ResponseEntity.noContent().build();
    }

    // ── Overtime Entries ──────────────────────────────────────────────────────

    @PostMapping("/{id}/overtime-entries")
    public ResponseEntity<OvertimeEntryResponse> addOvertimeEntry(@PathVariable Long id,
                                                                   @RequestBody AddOvertimeEntryBody body) {
        var response = addOvertimeEntry.execute(
                new AddOvertimeEntryRequest(body.incidentId(), body.overtimeHours(),
                        body.allowanceHours(), body.allowancePercentage(),
                        body.timeFrom(), body.timeTo(), body.isAllowanceEntry()));
        return ResponseEntity.created(
                URI.create("/api/v1/summaries/" + id + "/overtime-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}/overtime-entries/{entryId}")
    public ResponseEntity<OvertimeEntryResponse> overrideOvertimeEntry(@PathVariable Long id,
                                                                        @PathVariable Long entryId,
                                                                        @RequestBody OvertimeEntryOverrideBody body) {
        return ResponseEntity.ok(overrideOvertimeEntry.execute(
                new OverrideOvertimeEntryRequest(entryId, body.overtimeHours(),
                        body.allowanceHours(), body.allowancePercentage())));
    }

    @DeleteMapping("/{id}/overtime-entries/{entryId}")
    public ResponseEntity<Void> deleteOvertimeEntry(@PathVariable Long id,
                                                     @PathVariable Long entryId) {
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
