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
public class GetUserPreferencesUseCase implements UseCase<GetUserPreferencesRequest, UserPreferencesResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final GetUserPreferencesValidator validator;

    @Override
    public UserPreferencesResponse execute(GetUserPreferencesRequest request) {
        validator.validate(request);
        return preferencesGateway
                .find()
                .map(p -> new UserPreferencesResponse(p.colorScheme()))
                .orElse(new UserPreferencesResponse(ColorScheme.AUTO));
    }
}
