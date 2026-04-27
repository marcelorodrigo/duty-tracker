package com.dutytracker.application.usecase.preferences;

import com.dutytracker.domain.model.ColorScheme;

public record UpdateUserPreferencesRequest(ColorScheme colorScheme) {}
