package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.IncidentGateway;
import com.dutytracker.gateway.OnCallDayEntryGateway;
import com.dutytracker.gateway.OnCallPeriodGateway;
import com.dutytracker.gateway.OvertimeEntryGateway;
import com.dutytracker.gateway.RegistrationSummaryGateway;
import com.dutytracker.domain.model.Incident;
import com.dutytracker.domain.model.OnCallDayEntry;
import com.dutytracker.domain.model.OnCallPeriod;
import com.dutytracker.domain.model.OvertimeEntry;
import com.dutytracker.domain.model.RegistrationSummary;
import com.dutytracker.domain.model.StandbyRateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRegistrationSummaryUseCaseTest {

    @Mock RegistrationSummaryGateway registrationSummaryGateway;
    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock OvertimeEntryGateway overtimeEntryGateway;
    @Mock IncidentGateway incidentGateway;
    @Mock CreateRegistrationSummaryValidator validator;

    CreateRegistrationSummaryUseCase useCase;

    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2026, 4, 14, 0, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2026, 4, 20, 23, 59);
    private static final OnCallPeriod PERIOD = new OnCallPeriod(1L, PERIOD_START, PERIOD_END, Instant.now());

    @BeforeEach
    void setUp() {
        useCase = new CreateRegistrationSummaryUseCase(
                onCallPeriodGateway, registrationSummaryGateway,
                onCallDayEntryGateway, overtimeEntryGateway, incidentGateway, validator);
    }

    @Test
    @DisplayName("should create summary with auto-generated label when label is null")
    void shouldCreateSummaryWithAutoLabelWhenLabelIsNull() {
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(PERIOD));
        when(registrationSummaryGateway.save(any())).thenAnswer(inv -> {
            RegistrationSummary s = inv.getArgument(0);
            return new RegistrationSummary(10L, s.label(), s.periodStart(), s.periodEnd(), s.createdAt(), s.updatedAt());
        });
        when(onCallDayEntryGateway.findByOnCallPeriodId(1L)).thenReturn(List.of());
        when(incidentGateway.findByOnCallPeriodId(1L)).thenReturn(List.of());

        RegistrationSummaryResponse result = useCase.execute(new CreateRegistrationSummaryRequest(1L, null));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.label()).isEqualTo("Week of 2026-04-14 \u2013 2026-04-20");
        assertThat(result.periodStart()).isEqualTo(LocalDate.of(2026, 4, 14));
        assertThat(result.periodEnd()).isEqualTo(LocalDate.of(2026, 4, 20));
    }

    @Test
    @DisplayName("should create summary with provided label when label is given")
    void shouldCreateSummaryWithProvidedLabel() {
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(PERIOD));
        when(registrationSummaryGateway.save(any())).thenAnswer(inv -> {
            RegistrationSummary s = inv.getArgument(0);
            return new RegistrationSummary(11L, s.label(), s.periodStart(), s.periodEnd(), s.createdAt(), s.updatedAt());
        });
        when(onCallDayEntryGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(
                new OnCallDayEntry(5L, 1L, LocalDate.of(2026, 4, 14),
                        BigDecimal.valueOf(24), StandbyRateType.WEEKDAY_SATURDAY, false, false, false)));
        Incident incident = new Incident(20L, 1L, LocalDate.of(2026, 4, 15),
                LocalTime.of(2, 0), LocalTime.of(3, 0), Instant.now());
        when(incidentGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(incident));
        when(overtimeEntryGateway.findByIncidentId(20L)).thenReturn(List.of(
                new OvertimeEntry(30L, 20L, BigDecimal.ONE, null, null,
                        LocalTime.of(2, 0), LocalTime.of(3, 0), false, false)));

        RegistrationSummaryResponse result = useCase.execute(new CreateRegistrationSummaryRequest(1L, "My Label"));

        assertThat(result.label()).isEqualTo("My Label");
        assertThat(result.onCallEntries()).hasSize(1);
        assertThat(result.overtimeEntries()).hasSize(1);
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is not found")
    void shouldThrowWhenPeriodNotFound() {
        org.mockito.Mockito.doThrow(new InvalidOnCallPeriodException("Period not found"))
                .when(validator).validate(any());

        assertThatThrownBy(() -> useCase.execute(new CreateRegistrationSummaryRequest(99L, null)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("Period not found");
    }
}
