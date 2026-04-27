package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.oncall.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/oncall-periods")
public class OnCallPeriodController {

    private final CreateOnCallPeriodUseCase createPeriod;
    private final GetOnCallPeriodUseCase getPeriod;
    private final ListOnCallPeriodsUseCase listPeriods;
    private final UpdateOnCallPeriodUseCase updatePeriod;
    private final DeleteOnCallPeriodUseCase deletePeriod;
    private final AddHolidayOverrideUseCase addHoliday;
    private final RemoveHolidayOverrideUseCase removeHoliday;
    private final CalculateOnCallDayEntriesUseCase calculateEntries;
    private final OverrideOnCallDayEntryUseCase overrideDayEntry;

    public OnCallPeriodController(CreateOnCallPeriodUseCase createPeriod,
                                  GetOnCallPeriodUseCase getPeriod,
                                  ListOnCallPeriodsUseCase listPeriods,
                                  UpdateOnCallPeriodUseCase updatePeriod,
                                  DeleteOnCallPeriodUseCase deletePeriod,
                                  AddHolidayOverrideUseCase addHoliday,
                                  RemoveHolidayOverrideUseCase removeHoliday,
                                  CalculateOnCallDayEntriesUseCase calculateEntries,
                                  OverrideOnCallDayEntryUseCase overrideDayEntry) {
        this.createPeriod = createPeriod;
        this.getPeriod = getPeriod;
        this.listPeriods = listPeriods;
        this.updatePeriod = updatePeriod;
        this.deletePeriod = deletePeriod;
        this.addHoliday = addHoliday;
        this.removeHoliday = removeHoliday;
        this.calculateEntries = calculateEntries;
        this.overrideDayEntry = overrideDayEntry;
    }

    @PostMapping
    public ResponseEntity<OnCallPeriodResponse> create(@RequestBody CreateOnCallPeriodRequest request) {
        var response = createPeriod.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/oncall-periods/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<OnCallPeriodListResponse> list() {
        return ResponseEntity.ok(listPeriods.execute(new ListOnCallPeriodsRequest()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnCallPeriodResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(getPeriod.execute(new GetOnCallPeriodRequest(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OnCallPeriodResponse> update(@PathVariable Long id,
                                                       @RequestBody UpdateOnCallPeriodRequest request) {
        var req = new UpdateOnCallPeriodRequest(id, request.startDateTime(), request.endDateTime());
        return ResponseEntity.ok(updatePeriod.execute(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePeriod.execute(new DeleteOnCallPeriodRequest(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/holidays")
    public ResponseEntity<OnCallPeriodResponse> addHoliday(@PathVariable Long id,
                                                            @RequestBody AddHolidayBody body) {
        return ResponseEntity.ok(addHoliday.execute(new AddHolidayOverrideRequest(id, body.date())));
    }

    @DeleteMapping("/{id}/holidays/{date}")
    public ResponseEntity<Void> removeHoliday(@PathVariable Long id,
                                              @PathVariable LocalDate date) {
        removeHoliday.execute(new RemoveHolidayOverrideRequest(id, date));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<OnCallDayEntriesResponse> calculate(@PathVariable Long id) {
        return ResponseEntity.ok(calculateEntries.execute(new CalculateOnCallDayEntriesRequest(id)));
    }

    @PutMapping("/{periodId}/day-entries/{entryId}")
    public ResponseEntity<OnCallDayEntryResponse> overrideDayEntry(
            @PathVariable Long periodId,
            @PathVariable Long entryId,
            @RequestBody DayEntryOverrideBody body) {
        return ResponseEntity.ok(overrideDayEntry.execute(
                new OverrideOnCallDayEntryRequest(entryId, body.hours(), body.rateType(), body.timeForTimeFlag())));
    }

    record AddHolidayBody(LocalDate date) {}

    record DayEntryOverrideBody(java.math.BigDecimal hours, com.dutytracker.domain.model.StandbyRateType rateType, Boolean timeForTimeFlag) {}
}
