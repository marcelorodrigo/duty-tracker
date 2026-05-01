package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.DeleteIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.*;
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
        var result = useCase.execute(request);

        // then
        verify(incidentGateway).deleteById(7L);
        assertThat(result).isNull();
    }
}
