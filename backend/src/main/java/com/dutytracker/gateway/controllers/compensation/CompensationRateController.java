package com.dutytracker.gateway.controllers.compensation;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.usecase.compensation.*;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compensation-rates")
@Tag(name = "Compensation Rates", description = "Manage compensation rates and overtime pay calculations")
@RequiredArgsConstructor
public class CompensationRateController {
    private final GetCompensationRateTableUseCase getRates;
    private final CreateCompensationRateUseCase createRate;
    private final UpdateCompensationRateUseCase updateRate;
    private final DeleteCompensationRateUseCase deleteRate;

    @GetMapping
    @Operation(
            summary = "Get compensation rate table",
            description = "Retrieve compensation rates, optionally filtered by employee type")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Compensation rates retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CompensationRateTableResponse.class)))
            })
    public ResponseEntity<CompensationRateTableResponse> getAll(
            @Parameter(description = "Employee type filter (optional): PERMANENT or CONTRACTOR")
                    @RequestParam(required = false)
                    EmployeeType employeeType) {
        return ResponseEntity.ok(getRates.execute(new GetCompensationRateTableRequest(employeeType)));
    }

    @PostMapping
    @Operation(
            summary = "Create compensation rate",
            description = "Create a new compensation rate with percentage and label")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Compensation rate created successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CompensationRateResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid rate data")
            })
    public ResponseEntity<CompensationRateResponse> create(@RequestBody CreateCompensationRateRequest request) {
        var response = createRate.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/compensation-rates/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update compensation rate",
            description = "Update the percentage and label of a compensation rate")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Compensation rate updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CompensationRateResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid rate data"),
                @ApiResponse(responseCode = "404", description = "Compensation rate not found")
            })
    public ResponseEntity<CompensationRateResponse> update(
            @Parameter(description = "Compensation rate ID") @PathVariable Long id,
            @RequestBody UpdateCompensationRateRequest request) {
        var req = new UpdateCompensationRateRequest(id, request.percentage(), request.label());
        return ResponseEntity.ok(updateRate.execute(req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete compensation rate", description = "Remove a compensation rate from the system")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Compensation rate deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Compensation rate not found")
            })
    public ResponseEntity<Void> delete(@Parameter(description = "Compensation rate ID") @PathVariable Long id) {
        deleteRate.execute(new DeleteCompensationRateRequest(id));
        return ResponseEntity.noContent().build();
    }
}
