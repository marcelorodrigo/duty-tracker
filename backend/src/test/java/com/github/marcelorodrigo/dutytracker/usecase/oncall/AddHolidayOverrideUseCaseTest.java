package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayAlreadyRegisteredException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.AddHolidayOverrideRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.AddHolidayOverrideValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddHolidayOverrideUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayOverrideGateway holidayOverrideGateway;

    @Mock
    AddHolidayOverrideValidator validator;

    @InjectMocks
    AddHolidayOverrideUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);
    private static final LocalDate HOLIDAY = LocalDate.of(2026, 1, 8);

    @Test
    @DisplayName("should add holiday override and return updated period response")
    void shouldAddHolidayOverrideAndReturnUpdatedPeriodResponse() {
        // given
        var request = new AddHolidayOverrideRequest(1L, HOLIDAY);
        var period = new OnCallPeriod(1L, START, END, LocalDateTime.now());
        var savedOverride = new HolidayOverride(10L, 1L, HOLIDAY);
        when(holidayOverrideGateway.save(any())).thenReturn(savedOverride);
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(period));
        when(holidayOverrideGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(savedOverride));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.holidayOverrides()).containsExactly(HOLIDAY);
    }

    @Test
    @DisplayName("should throw HolidayAlreadyRegisteredException when override is duplicate")
    void shouldThrowHolidayAlreadyRegisteredExceptionWhenOverrideIsDuplicate() {
        // given
        var request = new AddHolidayOverrideRequest(1L, HOLIDAY);
        doThrow(new HolidayAlreadyRegisteredException()).when(validator).validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(HolidayAlreadyRegisteredException.class)
                .hasMessage("Holiday already registered for this date");
    }
}
