package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.profile.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile Management", description = "Manage engineer profiles and information")
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
    @Operation(summary = "Create engineer profile", description = "Create a new engineer profile with basic information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EngineerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Profile already exists")
    })
    public ResponseEntity<EngineerProfileResponse> create(@RequestBody CreateEngineerProfileRequest request) {
        var response = createProfile.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/profile")).body(response);
    }

    @GetMapping
    @Operation(summary = "Get engineer profile", description = "Retrieve the current user's engineer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EngineerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<EngineerProfileResponse> get() {
        return ResponseEntity.ok(getProfile.execute(new GetEngineerProfileRequest()));
    }

    @PutMapping
    @Operation(summary = "Update engineer profile", description = "Update the current user's engineer profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EngineerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<EngineerProfileResponse> update(@RequestBody UpdateEngineerProfileRequest request) {
        return ResponseEntity.ok(updateProfile.execute(request));
    }
}
