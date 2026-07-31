package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteEngineerProfileUseCase implements UseCase<DeleteEngineerProfileRequest, Void> {

    private final EngineerProfileGateway profileGateway;

    @Override
    @Transactional
    public Void execute(DeleteEngineerProfileRequest request) {
        var profile = profileGateway.find().orElseThrow(ProfileNotFoundException::new);
        profileGateway.deleteById(profile.id());
        return null;
    }
}
