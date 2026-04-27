package com.dutytracker.usecase.profile;

import static org.mockito.Mockito.verify;

import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.profile.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteEngineerProfileUseCaseTest {

    @Mock
    EngineerProfileGateway profileGateway;

    @Mock
    DeleteEngineerProfileValidator validator;

    @InjectMocks
    DeleteEngineerProfileUseCase useCase;

    @Test
    void callsValidatorAndGatewayDelete() {
        useCase.execute(new DeleteEngineerProfileRequest());

        verify(validator).validate(new DeleteEngineerProfileRequest());
        verify(profileGateway).delete();
    }
}
