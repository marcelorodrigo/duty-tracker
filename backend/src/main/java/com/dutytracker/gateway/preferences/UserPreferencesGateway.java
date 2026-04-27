package com.dutytracker.gateway.preferences;

import com.dutytracker.domain.UserPreferences;

import java.util.Optional;

public interface UserPreferencesGateway {
    UserPreferences save(UserPreferences preferences);
    Optional<UserPreferences> find();
}
