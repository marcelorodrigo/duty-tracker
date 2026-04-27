package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.onboarding.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
    private final GetOnboardingStatusUseCase getStatus;
    private final AdvanceOnboardingStepUseCase advanceStep;

    public OnboardingController(GetOnboardingStatusUseCase getStatus, AdvanceOnboardingStepUseCase advanceStep) {
        this.getStatus = getStatus;
        this.advanceStep = advanceStep;
    }

    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusResponse> getStatus() {
        return ResponseEntity.ok(getStatus.execute(new GetOnboardingStatusRequest()));
    }

    @PostMapping("/advance")
    public ResponseEntity<OnboardingStatusResponse> advance(@RequestBody AdvanceOnboardingStepRequest request) {
        return ResponseEntity.ok(advanceStep.execute(request));
    }
}
