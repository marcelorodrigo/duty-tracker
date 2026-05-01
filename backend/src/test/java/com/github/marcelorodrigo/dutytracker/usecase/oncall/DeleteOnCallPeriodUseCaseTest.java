package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.DeleteOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.DeleteOnCallPeriodValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteOnCallPeriodUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    DeleteOnCallPeriodValidator validator;

    @InjectMocks
    DeleteOnCallPeriodUseCase useCase;

    @Test
    @DisplayName("should call deleteById with the correct period id")
    void shouldCallDeleteByIdWithTheCorrectPeriodId() {
        // given
        var request = new DeleteOnCallPeriodRequest(5L);

        // when
        useCase.execute(request);

        // then
        verify(onCallPeriodGateway).deleteById(5L);
    }
}
