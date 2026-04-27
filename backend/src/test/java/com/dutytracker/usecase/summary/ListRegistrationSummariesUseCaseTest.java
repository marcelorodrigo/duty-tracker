package com.dutytracker.usecase.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.response.summary.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRegistrationSummariesUseCaseTest {

    @Mock
    RegistrationSummaryGateway registrationSummaryGateway;

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    OnCallDayEntryGateway onCallDayEntryGateway;

    @Mock
    OvertimeEntryGateway overtimeEntryGateway;

    @Mock
    IncidentGateway incidentGateway;

    ListRegistrationSummariesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListRegistrationSummariesUseCase(
                registrationSummaryGateway,
                onCallPeriodGateway,
                onCallDayEntryGateway,
                overtimeEntryGateway,
                incidentGateway);
    }

    @Test
    @DisplayName("should return list of registration summaries")
    void shouldReturnListOfSummaries() {
        LocalDate start = LocalDate.of(2026, 4, 14);
        LocalDate end = LocalDate.of(2026, 4, 20);
        RegistrationSummary summary = new RegistrationSummary(1L, "Week 1", start, end, Instant.now(), Instant.now());
        OnCallPeriod period = new OnCallPeriod(
                10L, LocalDateTime.of(2026, 4, 14, 0, 0), LocalDateTime.of(2026, 4, 20, 23, 59), Instant.now());

        when(registrationSummaryGateway.findAll()).thenReturn(List.of(summary));
        when(onCallPeriodGateway.findAll()).thenReturn(List.of(period));
        when(onCallDayEntryGateway.findByOnCallPeriodId(10L)).thenReturn(List.of());
        when(incidentGateway.findByOnCallPeriodId(10L)).thenReturn(List.of());

        RegistrationSummaryListResponse result = useCase.execute(new ListRegistrationSummariesRequest());

        assertThat(result.summaries()).hasSize(1);
        assertThat(result.summaries().get(0).id()).isEqualTo(1L);
        assertThat(result.summaries().get(0).label()).isEqualTo("Week 1");
    }

    @Test
    @DisplayName("should return empty list when no summaries exist")
    void shouldReturnEmptyListWhenNoSummaries() {
        when(registrationSummaryGateway.findAll()).thenReturn(List.of());
        when(onCallPeriodGateway.findAll()).thenReturn(List.of());

        RegistrationSummaryListResponse result = useCase.execute(new ListRegistrationSummariesRequest());

        assertThat(result.summaries()).isEmpty();
    }
}
