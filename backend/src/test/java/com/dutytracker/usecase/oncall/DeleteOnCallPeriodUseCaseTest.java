package com.dutytracker.usecase.oncall;
import com.dutytracker.usecase.validator.oncall.*;



import com.dutytracker.domain.*;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class DeleteOnCallPeriodUseCaseTest {

    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock DeleteOnCallPeriodValidator validator;
    @InjectMocks DeleteOnCallPeriodUseCase useCase;

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
