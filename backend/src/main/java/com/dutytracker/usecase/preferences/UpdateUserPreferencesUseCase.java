package com.dutytracker.usecase.preferences;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.response.preferences.*;
import com.dutytracker.usecase.validator.preferences.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateUserPreferencesUseCase implements UseCase<UpdateUserPreferencesRequest, UserPreferencesResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final UpdateUserPreferencesValidator validator;

    @Override
    public UserPreferencesResponse execute(UpdateUserPreferencesRequest request) {
        validator.validate(request);
        UserPreferences existing = preferencesGateway.find().orElse(new UserPreferences(null, request.colorScheme()));
        UserPreferences updated = new UserPreferences(existing.id(), request.colorScheme());
        UserPreferences saved = preferencesGateway.save(updated);
        return new UserPreferencesResponse(saved.colorScheme());
    }
}
