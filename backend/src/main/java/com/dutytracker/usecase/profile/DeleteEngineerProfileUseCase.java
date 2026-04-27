package com.dutytracker.usecase.profile;

import com.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteEngineerProfileUseCase implements UseCase<DeleteEngineerProfileRequest, Void> {

    private final EngineerProfileGateway profileGateway;
    private final DeleteEngineerProfileValidator validator;

    @Override
    public Void execute(DeleteEngineerProfileRequest request) {
        validator.validate(request);
        var profile = profileGateway.find().orElseThrow(ProfileNotFoundException::new);
        profileGateway.deleteById(profile.id());
        return null;
    }
}
