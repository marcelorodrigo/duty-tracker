package com.dutytracker.application.usecase.oncall;

import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.model.OnCallDayEntry;
import com.dutytracker.domain.model.StandbyRateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverrideOnCallDayEntryUseCaseTest {

    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock OverrideOnCallDayEntryValidator validator;
    @InjectMocks OverrideOnCallDayEntryUseCase useCase;

    private static final LocalDate DATE = LocalDate.of(2026, 4, 15);

    private OnCallDayEntry existingEntry() {
        return new OnCallDayEntry(
                10L, 1L, DATE,
                new BigDecimal("8.0000"),
                StandbyRateType.WEEKDAY_SATURDAY,
                false, false, false
        );
    }

    @Test
    @DisplayName("flag-only override sets timeForTimeFlag and manualOverride, keeps existing hours and rateType")
    void shouldOverrideFlagOnly() {
        // given
        var request = new OverrideOnCallDayEntryRequest(10L, null, null, true);
        var existing = existingEntry();
        when(onCallDayEntryGateway.findById(10L)).thenReturn(Optional.of(existing));
        when(onCallDayEntryGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.timeForTimeFlag()).isTrue();
        assertThat(result.manualOverride()).isTrue();
        assertThat(result.hours()).isEqualByComparingTo("8.0000");
        assertThat(result.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
    }

    @Test
    @DisplayName("full override replaces hours, rateType, timeForTimeFlag and sets manualOverride")
    void shouldOverrideAllFields() {
        // given
        var request = new OverrideOnCallDayEntryRequest(
                10L, new BigDecimal("4.00"), StandbyRateType.SUNDAY_HOLIDAY, false);
        var existing = existingEntry();
        when(onCallDayEntryGateway.findById(10L)).thenReturn(Optional.of(existing));
        when(onCallDayEntryGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.hours()).isEqualByComparingTo("4.00");
        assertThat(result.rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);
        assertThat(result.timeForTimeFlag()).isFalse();
        assertThat(result.manualOverride()).isTrue();
    }

    @Test
    @DisplayName("throws InvalidOnCallPeriodException when entry not found")
    void shouldThrowWhenEntryNotFound() {
        // given
        var request = new OverrideOnCallDayEntryRequest(99L, null, null, true);
        doThrow(new InvalidOnCallPeriodException("Day entry not found"))
                .when(validator).validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("Day entry not found");
    }
}
