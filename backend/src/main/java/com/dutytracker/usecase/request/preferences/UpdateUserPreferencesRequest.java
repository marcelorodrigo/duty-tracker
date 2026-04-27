package com.dutytracker.usecase.request.preferences;

import com.dutytracker.domain.model.ColorScheme;

public record UpdateUserPreferencesRequest(ColorScheme colorScheme) {}
