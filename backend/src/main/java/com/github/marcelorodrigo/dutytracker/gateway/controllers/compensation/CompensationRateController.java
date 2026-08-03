package com.github.marcelorodrigo.dutytracker.gateway.controllers.compensation;

import com.github.marcelorodrigo.dutytracker.gateway.api.CompensationRatesApi;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.CreateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.DeleteCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.GetCompensationRateTableUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.UpdateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateTableResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompensationRateController implements CompensationRatesApi {
    private static final String COMPENSATION_RATE_ID = "compensationRateId";
    private final GetCompensationRateTableUseCase getRates;
    private final CreateCompensationRateUseCase createRate;
    private final UpdateCompensationRateUseCase updateRate;
    private final DeleteCompensationRateUseCase deleteRate;

    @Override
    public ResponseEntity<CompensationRateTableResponse> getAllCompensationRates() {
        return ResponseEntity.ok(getRates.execute(new GetCompensationRateTableRequest()));
    }

    @Override
    public ResponseEntity<CompensationRateResponse> createCompensationRate(
            CreateCompensationRateRequest createCompensationRateRequest) {
        var response = createRate.execute(createCompensationRateRequest);
        log.atInfo()
                .addKeyValue(COMPENSATION_RATE_ID, response.id())
                .addKeyValue("overtimeDayType", createCompensationRateRequest.overtimeDayType())
                .addKeyValue("label", createCompensationRateRequest.label())
                .log("Compensation rate created");
        return ResponseEntity.created(URI.create("/api/v1/compensation-rates/" + response.id()))
                .body(response);
    }

    @Override
    public ResponseEntity<CompensationRateResponse> updateCompensationRate(
            Long id, UpdateCompensationRateRequest updateCompensationRateRequest) {
        var req = new UpdateCompensationRateRequest(
                id, updateCompensationRateRequest.percentage(), updateCompensationRateRequest.label());
        var response = updateRate.execute(req);
        log.atInfo().addKeyValue(COMPENSATION_RATE_ID, response.id()).log("Compensation rate updated");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteCompensationRate(Long id) {
        deleteRate.execute(new DeleteCompensationRateRequest(id));
        log.atInfo().addKeyValue(COMPENSATION_RATE_ID, id).log("Compensation rate deleted");
        return ResponseEntity.noContent().build();
    }
}
