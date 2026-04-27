package com.dutytracker.application.usecase.summary;

import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.IncidentGateway;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import com.dutytracker.domain.gateway.RegistrationSummaryGateway;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRegistrationSummaryUseCaseTest {

    @Mock RegistrationSummaryGateway registrationSummaryGateway;
    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock OvertimeEntryGateway overtimeEntryGateway;
    @Mock IncidentGateway incidentGateway;
    @Mock GetRegistrationSummaryValidator validator;

    GetRegistrationSummaryUseCase useCase;

    private static final LocalDate START_DATE = LocalDate.of(2026, 4, 14);
    private static final LocalDate END_DATE = LocalDate.of(2026, 4, 20);
    private static final RegistrationSummary SUMMARY = new RegistrationSummary(
            1L, "Week label", START_DATE, END_DATE, Instant.now(), Instant.now());
    private static final OnCallPeriod PERIOD = new OnCallPeriod(
            10L,
            LocalDateTime.of(2026, 4, 14, 0, 0),
            LocalDateTime.of(2026, 4, 20, 23, 59),
            Instant.now());

    @BeforeEach
    void setUp() {
        useCase = new GetRegistrationSummaryUseCase(
                registrationSummaryGateway, onCallPeriodGateway,
                onCallDayEntryGateway, overtimeEntryGateway, incidentGateway, validator);
    }

    @Test
    @DisplayName("should return summary with entries when summary and matching period exist")
    void shouldReturnSummaryWithEntries() {
        when(registrationSummaryGateway.findById(1L)).thenReturn(Optional.of(SUMMARY));
        when(onCallPeriodGateway.findAll()).thenReturn(List.of(PERIOD));
        when(onCallDayEntryGateway.findByOnCallPeriodId(10L)).thenReturn(List.of(
                new OnCallDayEntry(5L, 10L, START_DATE, BigDecimal.valueOf(24),
                        StandbyRateType.WEEKDAY_SATURDAY, false, false, false)));
        Incident incident = new Incident(20L, 10L, START_DATE,
                LocalTime.of(2, 0), LocalTime.of(3, 0), Instant.now());
        when(incidentGateway.findByOnCallPeriodId(10L)).thenReturn(List.of(incident));
        when(overtimeEntryGateway.findByIncidentId(20L)).thenReturn(List.of(
                new OvertimeEntry(30L, 20L, BigDecimal.ONE, null, null,
                        LocalTime.of(2, 0), LocalTime.of(3, 0), false, false)));

        RegistrationSummaryResponse result = useCase.execute(new GetRegistrationSummaryRequest(1L));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.label()).isEqualTo("Week label");
        assertThat(result.onCallEntries()).hasSize(1);
        assertThat(result.overtimeEntries()).hasSize(1);
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when summary is not found")
    void shouldThrowWhenSummaryNotFound() {
        when(registrationSummaryGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetRegistrationSummaryRequest(99L)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("Summary not found");
    }
}
