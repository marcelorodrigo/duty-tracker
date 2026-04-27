package com.dutytracker.gateway.controllers.preferences;

import com.dutytracker.usecase.preferences.*;
import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.response.preferences.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@Tag(name = "Preferences", description = "Manage user preferences and settings")
@RequiredArgsConstructor
public class PreferencesController {
    private final GetUserPreferencesUseCase getPreferences;
    private final UpdateUserPreferencesUseCase updatePreferences;

    @GetMapping
    @Operation(summary = "Get user preferences", description = "Retrieve the current user's preferences and settings")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Preferences retrieved successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UserPreferencesResponse.class))),
                @ApiResponse(responseCode = "404", description = "Preferences not found")
            })
    public ResponseEntity<UserPreferencesResponse> get() {
        return ResponseEntity.ok(getPreferences.execute(new GetUserPreferencesRequest()));
    }

    @PutMapping
    @Operation(summary = "Update user preferences", description = "Update the current user's preferences and settings")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Preferences updated successfully",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = UserPreferencesResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid preference data")
            })
    public ResponseEntity<UserPreferencesResponse> update(@RequestBody UpdateUserPreferencesRequest request) {
        return ResponseEntity.ok(updatePreferences.execute(request));
    }
}
