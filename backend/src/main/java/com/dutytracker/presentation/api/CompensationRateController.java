package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.compensation.*;
import com.dutytracker.domain.model.EmployeeType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/compensation-rates")
public class CompensationRateController {
    private final GetCompensationRateTableUseCase getRates;
    private final CreateCompensationRateUseCase createRate;
    private final UpdateCompensationRateUseCase updateRate;
    private final DeleteCompensationRateUseCase deleteRate;

    public CompensationRateController(GetCompensationRateTableUseCase getRates,
                                       CreateCompensationRateUseCase createRate,
                                       UpdateCompensationRateUseCase updateRate,
                                       DeleteCompensationRateUseCase deleteRate) {
        this.getRates = getRates;
        this.createRate = createRate;
        this.updateRate = updateRate;
        this.deleteRate = deleteRate;
    }

    @GetMapping
    public ResponseEntity<CompensationRateTableResponse> getAll(
            @RequestParam(required = false) EmployeeType employeeType) {
        return ResponseEntity.ok(getRates.execute(new GetCompensationRateTableRequest(employeeType)));
    }

    @PostMapping
    public ResponseEntity<CompensationRateResponse> create(@RequestBody CreateCompensationRateRequest request) {
        var response = createRate.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/compensation-rates/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompensationRateResponse> update(@PathVariable Long id,
                                                            @RequestBody UpdateCompensationRateRequest request) {
        var req = new UpdateCompensationRateRequest(id, request.percentage(), request.label());
        return ResponseEntity.ok(updateRate.execute(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRate.execute(new DeleteCompensationRateRequest(id));
        return ResponseEntity.noContent().build();
    }
}
