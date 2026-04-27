package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.profile.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final CreateEngineerProfileUseCase createProfile;
    private final GetEngineerProfileUseCase getProfile;
    private final UpdateEngineerProfileUseCase updateProfile;

    public ProfileController(CreateEngineerProfileUseCase createProfile,
                             GetEngineerProfileUseCase getProfile,
                             UpdateEngineerProfileUseCase updateProfile) {
        this.createProfile = createProfile;
        this.getProfile = getProfile;
        this.updateProfile = updateProfile;
    }

    @PostMapping
    public ResponseEntity<EngineerProfileResponse> create(@RequestBody CreateEngineerProfileRequest request) {
        var response = createProfile.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/profile")).body(response);
    }

    @GetMapping
    public ResponseEntity<EngineerProfileResponse> get() {
        return ResponseEntity.ok(getProfile.execute(new GetEngineerProfileRequest()));
    }

    @PutMapping
    public ResponseEntity<EngineerProfileResponse> update(@RequestBody UpdateEngineerProfileRequest request) {
        return ResponseEntity.ok(updateProfile.execute(request));
    }
}
