package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.preferences.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferencesController {
    private final GetUserPreferencesUseCase getPreferences;
    private final UpdateUserPreferencesUseCase updatePreferences;

    public PreferencesController(GetUserPreferencesUseCase getPreferences,
                                  UpdateUserPreferencesUseCase updatePreferences) {
        this.getPreferences = getPreferences;
        this.updatePreferences = updatePreferences;
    }

    @GetMapping
    public ResponseEntity<UserPreferencesResponse> get() {
        return ResponseEntity.ok(getPreferences.execute(new GetUserPreferencesRequest()));
    }

    @PutMapping
    public ResponseEntity<UserPreferencesResponse> update(@RequestBody UpdateUserPreferencesRequest request) {
        return ResponseEntity.ok(updatePreferences.execute(request));
    }
}
