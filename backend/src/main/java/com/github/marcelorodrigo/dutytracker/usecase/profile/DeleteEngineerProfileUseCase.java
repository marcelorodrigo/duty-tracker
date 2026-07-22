package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.CommandUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.profile.DeleteEngineerProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteEngineerProfileUseCase implements CommandUseCase<DeleteEngineerProfileRequest> {

    private final EngineerProfileGateway profileGateway;
    private final DeleteEngineerProfileValidator validator;

    @Override
    public void execute(DeleteEngineerProfileRequest request) {
        validator.validate(request);
        var profile = profileGateway.find().orElseThrow(ProfileNotFoundException::new);
        profileGateway.deleteById(profile.id());
    }
}
