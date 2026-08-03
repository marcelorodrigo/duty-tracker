package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.GetEngineerProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEngineerProfileUseCase implements UseCase<GetEngineerProfileRequest, EngineerProfileResponse> {

    private final EngineerProfileGateway profileGateway;
    private final GetEngineerProfileValidator validator;
    private final EngineerProfileResponseMapper responseMapper;

    @Override
    public EngineerProfileResponse execute(GetEngineerProfileRequest request) {
        validator.validate(request);
        return profileGateway.find().map(responseMapper::toResponse).orElse(null);
    }
}
