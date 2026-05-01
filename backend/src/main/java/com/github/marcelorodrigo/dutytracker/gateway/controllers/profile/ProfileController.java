package com.github.marcelorodrigo.dutytracker.gateway.controllers.profile;

import com.github.marcelorodrigo.dutytracker.usecase.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.profile.CreateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.DeleteEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.GetEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.UpdateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile Management", description = "Manage engineer profiles and information")
@RequiredArgsConstructor
public class ProfileController {
    private final CreateEngineerProfileUseCase createProfile;
    private final GetEngineerProfileUseCase getProfile;
    private final UpdateEngineerProfileUseCase updateProfile;
    private final DeleteEngineerProfileUseCase deleteProfile;

    @PostMapping
    @Operation(
            summary = "Create engineer profile",
            description = "Create a new engineer profile with basic information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Profile created successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = EngineerProfileResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "409", description = "Profile already exists")
            })
    public ResponseEntity<EngineerProfileResponse> create(@RequestBody CreateEngineerProfileRequest request) {
        var response = createProfile.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/profile")).body(response);
    }

    @GetMapping
    @Operation(summary = "Get engineer profile", description = "Retrieve the current user's engineer profile")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Profile retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = EngineerProfileResponse.class))),
                @ApiResponse(responseCode = "404", description = "Profile not found")
            })
    public ResponseEntity<EngineerProfileResponse> get() {
        var response = getProfile.execute(new GetEngineerProfileRequest());
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
            summary = "Update engineer profile",
            description = "Update the current user's engineer profile information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Profile updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = EngineerProfileResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "404", description = "Profile not found")
            })
    public ResponseEntity<EngineerProfileResponse> update(@RequestBody UpdateEngineerProfileRequest request) {
        return ResponseEntity.ok(updateProfile.execute(request));
    }

    @DeleteMapping
    @Operation(summary = "Delete engineer profile", description = "Delete the current user's engineer profile")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Profile not found")
            })
    public ResponseEntity<Void> delete() {
        deleteProfile.execute(new DeleteEngineerProfileRequest());
        return ResponseEntity.noContent().build();
    }
}
