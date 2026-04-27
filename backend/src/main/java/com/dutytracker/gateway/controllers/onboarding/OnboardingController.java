package com.dutytracker.gateway.controllers.onboarding;


import com.dutytracker.usecase.onboarding.*;
import com.dutytracker.usecase.request.onboarding.*;
import com.dutytracker.usecase.response.onboarding.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Onboarding", description = "Manage engineer onboarding process and workflow")
public class OnboardingController {
    private final GetOnboardingStatusUseCase getStatus;
    private final AdvanceOnboardingStepUseCase advanceStep;

    public OnboardingController(GetOnboardingStatusUseCase getStatus, AdvanceOnboardingStepUseCase advanceStep) {
        this.getStatus = getStatus;
        this.advanceStep = advanceStep;
    }

    @GetMapping
    @Operation(summary = "Get onboarding status", description = "Retrieve the current onboarding status and completed steps")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboarding status retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnboardingStatusResponse.class)))
    })
    public ResponseEntity<OnboardingStatusResponse> get() {
        return ResponseEntity.ok(getStatus.execute(new GetOnboardingStatusRequest()));
    }

    @PostMapping
    @Operation(summary = "Advance onboarding step", description = "Progress to the next step in the onboarding workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboarding step advanced successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnboardingStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid step data or unable to advance")
    })
    public ResponseEntity<OnboardingStatusResponse> post(@RequestBody AdvanceOnboardingStepRequest request) {
        return ResponseEntity.ok(advanceStep.execute(request));
    }
}
