package com.dutytracker.usecase.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OverrideOvertimeEntryUseCaseTest {

    @Mock
    OvertimeEntryGateway overtimeEntryGateway;

    @Mock
    OverrideOvertimeEntryValidator validator;

    OverrideOvertimeEntryUseCase useCase;

    private static final OvertimeEntry EXISTING = new OvertimeEntry(
            1L,
            10L,
            new BigDecimal("2.0000"),
            new BigDecimal("2.0000"),
            new BigDecimal("50.00"),
            LocalTime.of(22, 0),
            LocalTime.of(23, 0),
            false,
            false);

    @BeforeEach
    void setUp() {
        useCase = new OverrideOvertimeEntryUseCase(overtimeEntryGateway, validator);
    }

    @Test
    @DisplayName("should override overtime fields and set manualOverride to true")
    void shouldOverrideFieldsAndSetManualOverride() {
        when(overtimeEntryGateway.findById(1L)).thenReturn(Optional.of(EXISTING));
        when(overtimeEntryGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OverrideOvertimeEntryRequest request = new OverrideOvertimeEntryRequest(
                1L, new BigDecimal("3.0000"), new BigDecimal("3.0000"), new BigDecimal("75.00"));

        OvertimeEntryResponse result = useCase.execute(request);

        assertThat(result.overtimeHours()).isEqualByComparingTo(new BigDecimal("3.0000"));
        assertThat(result.allowanceHours()).isEqualByComparingTo(new BigDecimal("3.0000"));
        assertThat(result.allowancePercentage()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.manualOverride()).isTrue();
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when entry is not found")
    void shouldThrowWhenEntryNotFound() {
        doThrow(new InvalidOnCallPeriodException("Overtime entry not found"))
                .when(validator)
                .validate(any());

        assertThatThrownBy(() -> useCase.execute(new OverrideOvertimeEntryRequest(99L, null, null, null)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("Overtime entry not found");
    }
}
