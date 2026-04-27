package com.dutytracker.usecase.summary;






import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class AddOnCallDayEntryUseCaseTest {

    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock AddOnCallDayEntryValidator validator;

    AddOnCallDayEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddOnCallDayEntryUseCase(onCallDayEntryGateway, validator);
    }

    @Test
    @DisplayName("should add on-call day entry with manualOverride set to true")
    void shouldAddEntryWithManualOverrideTrue() {
        LocalDate date = LocalDate.of(2026, 4, 14);
        AddOnCallDayEntryRequest request = new AddOnCallDayEntryRequest(
                1L, date, BigDecimal.valueOf(24), StandbyRateType.WEEKDAY_SATURDAY);

        when(onCallDayEntryGateway.save(any())).thenAnswer(inv -> {
            OnCallDayEntry e = inv.getArgument(0);
            return new OnCallDayEntry(50L, e.onCallPeriodId(), e.date(), e.hours(),
                    e.rateType(), e.capped(), e.timeForTimeFlag(), e.manualOverride());
        });

        OnCallDayEntryResponse result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(50L);
        assertThat(result.manualOverride()).isTrue();
        assertThat(result.capped()).isFalse();
        assertThat(result.timeForTimeFlag()).isFalse();
        assertThat(result.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is not found")
    void shouldThrowWhenPeriodNotFound() {
        doThrow(new InvalidOnCallPeriodException("Period not found"))
                .when(validator).validate(any());

        assertThatThrownBy(() -> useCase.execute(
                new AddOnCallDayEntryRequest(99L, LocalDate.now(), BigDecimal.ONE, StandbyRateType.WEEKDAY_SATURDAY)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("Period not found");
    }
}
