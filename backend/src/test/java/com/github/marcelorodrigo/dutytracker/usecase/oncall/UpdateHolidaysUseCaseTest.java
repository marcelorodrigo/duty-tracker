package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.UpdateHolidaysValidator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateHolidaysUseCaseTest {

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private HolidayGateway holidayGateway;

    @Mock
    private UpdateHolidaysValidator validator;

    @InjectMocks
    private UpdateHolidaysUseCase useCase;

    @Test
    @DisplayName("should throw on-call period not found exception when period is missing")
    void shouldThrowOnCallPeriodNotFoundExceptionWhenPeriodIsMissing() {
        // given
        var request = new UpdateHolidaysRequest(99L, List.of());
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(OnCallPeriodNotFoundException.class)
                .hasMessage("On-call period not found: 99");
    }
}
