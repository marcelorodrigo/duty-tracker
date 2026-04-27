package com.dutytracker.usecase.incident;

import com.dutytracker.gateway.IncidentGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import com.dutytracker.domain.*;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;

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
