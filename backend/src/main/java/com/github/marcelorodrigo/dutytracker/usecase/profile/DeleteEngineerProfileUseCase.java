package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.DeleteEngineerProfileValidator;
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
