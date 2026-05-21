package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import com.github.marcelorodrigo.dutytracker.gateway.api.OnCallPeriodsApi;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.CalculateEarningsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.CalculateOnCallDayEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.CreateOnCallPeriodUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.DeleteOnCallPeriodUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GenerateOnCallPeriodReportUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetOnCallPeriodHolidaysUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetOnCallPeriodUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.ListOnCallPeriodsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.UpdateHolidaysUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.UpdateOnCallPeriodUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.DeleteOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.EarningsResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OnCallPeriodController implements OnCallPeriodsApi {

    private final CreateOnCallPeriodUseCase createPeriod;
    private final GetOnCallPeriodUseCase getPeriod;
    private final ListOnCallPeriodsUseCase listPeriods;
    private final UpdateOnCallPeriodUseCase updatePeriod;
    private final DeleteOnCallPeriodUseCase deletePeriod;
    private final GetOnCallPeriodHolidaysUseCase getHolidays;
    private final UpdateHolidaysUseCase updateHolidays;
    private final CalculateOnCallDayEntriesUseCase calculateEntries;
    private final GenerateOnCallPeriodReportUseCase generateReport;
    private final CalculateEarningsUseCase calculateEarnings;

    @Override
    public ResponseEntity<OnCallPeriodResponse> createOnCallPeriod(
            CreateOnCallPeriodRequest createOnCallPeriodRequest) {
        var response = createPeriod.execute(createOnCallPeriodRequest);
        log.atInfo()
                .addKeyValue("onCallPeriodId", response.id())
                .addKeyValue("startDateTime", createOnCallPeriodRequest.startDateTime())
                .addKeyValue("endDateTime", createOnCallPeriodRequest.endDateTime())
                .log("On-call period created");
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<OnCallPeriodListResponse> listOnCallPeriods() {
        return ResponseEntity.ok(listPeriods.execute(new ListOnCallPeriodsRequest()));
    }

    @Override
    public ResponseEntity<OnCallPeriodResponse> getOnCallPeriod(Long id) {
        return ResponseEntity.ok(getPeriod.execute(new GetOnCallPeriodRequest(id)));
    }

    @Override
    public ResponseEntity<OnCallPeriodResponse> updateOnCallPeriod(
            Long id, UpdateOnCallPeriodRequest updateOnCallPeriodRequest) {
        var req = new UpdateOnCallPeriodRequest(
                id, updateOnCallPeriodRequest.startDateTime(), updateOnCallPeriodRequest.endDateTime());
        var response = updatePeriod.execute(req);
        log.atInfo()
                .addKeyValue("onCallPeriodId", id)
                .addKeyValue("startDateTime", updateOnCallPeriodRequest.startDateTime())
                .addKeyValue("endDateTime", updateOnCallPeriodRequest.endDateTime())
                .log("On-call period updated");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteOnCallPeriod(Long id) {
        deletePeriod.execute(new DeleteOnCallPeriodRequest(id));
        log.atInfo().addKeyValue("onCallPeriodId", id).log("On-call period deleted");
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<HolidayResponse>> getOnCallPeriodHolidays(Long id) {
        return ResponseEntity.ok(getHolidays.execute(new GetOnCallPeriodHolidaysRequest(id)));
    }

    @Override
    public ResponseEntity<List<HolidayResponse>> updateOnCallPeriodHolidays(
            Long id, List<HolidayResponse> holidayResponse) {
        var response = updateHolidays.execute(new UpdateHolidaysRequest(id, holidayResponse));
        log.atInfo()
                .addKeyValue("onCallPeriodId", id)
                .addKeyValue("holidayCount", holidayResponse.size())
                .log("On-call period holidays updated");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<OnCallDayEntriesResponse> calculateOnCallDayEntries(Long id) {
        log.atInfo().addKeyValue("onCallPeriodId", id).log("On-call day entries calculation requested");
        return ResponseEntity.ok(calculateEntries.execute(new CalculateOnCallDayEntriesRequest(id)));
    }

    @Override
    public ResponseEntity<OnCallPeriodReportResponse> getOnCallPeriodReport(Long id) {
        log.atInfo().addKeyValue("onCallPeriodId", id).log("On-call period report generation requested");
        return ResponseEntity.ok(generateReport.execute(new GenerateOnCallPeriodReportRequest(id)));
    }

    @Override
    public ResponseEntity<EarningsResponse> getOnCallPeriodEarnings(Long id) {
        log.atInfo().addKeyValue("onCallPeriodId", id).log("On-call period earnings calculation requested");
        return ResponseEntity.ok(calculateEarnings.execute(new CalculateEarningsRequest(id)));
    }
}
