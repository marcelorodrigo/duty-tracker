package com.dutytracker.usecase.profile;

import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteEngineerProfileUseCase implements UseCase<DeleteEngineerProfileRequest, Void> {

    private final EngineerProfileGateway profileGateway;
    private final DeleteEngineerProfileValidator validator;

    @Override
    public Void execute(DeleteEngineerProfileRequest request) {
        validator.validate(request);
        profileGateway.delete();
        return null;
    }
}
