package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.DeleteIncidentValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    DeleteIncidentValidator validator;

    @InjectMocks
    DeleteIncidentUseCase useCase;

    @Test
    @DisplayName("should call deleteById with the given incidentId")
    void shouldCallDeleteByIdWithGivenIncidentId() {
        // given
        var request = new DeleteIncidentRequest(7L);

        // when
        useCase.execute(request);

        // then
        verify(incidentGateway).deleteById(7L);
    }
}
