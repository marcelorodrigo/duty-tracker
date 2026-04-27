package com.dutytracker.domain.gateway;

import com.dutytracker.domain.model.UserPreferences;

import java.util.Optional;

public interface UserPreferencesGateway {
    UserPreferences save(UserPreferences preferences);
    Optional<UserPreferences> find();
}
