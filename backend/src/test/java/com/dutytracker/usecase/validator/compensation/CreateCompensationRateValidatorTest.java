package com.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCompensationRateValidatorTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @InjectMocks
    CreateCompensationRateValidator validator;

    @Test
    @DisplayName("should throw IllegalArgumentException when overtimeDayType is null")
    void shouldThrowWhenOvertimeDayTypeIsNull() {
        CreateCompensationRateRequest request = new CreateCompensationRateRequest(
                EmployeeType.INTERNAL,
                null,
                "Evening",
                LocalTime.of(18, 0),
                LocalTime.of(22, 0),
                new BigDecimal("35.00"));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overtimeDayType");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when timeFrom is null")
    void shouldThrowWhenTimeFromIsNull() {
        CreateCompensationRateRequest request = new CreateCompensationRateRequest(
                EmployeeType.INTERNAL,
                OvertimeDayType.WEEKDAY,
                "Evening",
                null,
                LocalTime.of(22, 0),
                new BigDecimal("35.00"));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timeFrom");
    }

    @Test
    @DisplayName("should throw DuplicateCompensationRateException when exact duplicate exists")
    void shouldThrowWhenExactDuplicateExists() {
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(22, 0);

        CompensationRate existing = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Evening",
                from,
                to,
                new BigDecimal("35.00"));

        when(compensationRateGateway.findByEmployeeTypeAndRateCategoryAndOvertimeDayType(
                        EmployeeType.INTERNAL, RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(List.of(existing));

        CreateCompensationRateRequest request = new CreateCompensationRateRequest(
                EmployeeType.INTERNAL, OvertimeDayType.WEEKDAY, "Evening duplicate", from, to, new BigDecimal("35.00"));

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(DuplicateCompensationRateException.class);
    }

    @Test
    @DisplayName("should pass validation when same time window exists but with a different overtimeDayType")
    void shouldPassWhenSameTimeWindowButDifferentDayType() {
        LocalTime from = LocalTime.of(22, 0);
        LocalTime to = LocalTime.MIDNIGHT;

        CompensationRate existing = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Weekday night",
                from,
                to,
                new BigDecimal("50.00"));

        when(compensationRateGateway.findByEmployeeTypeAndRateCategoryAndOvertimeDayType(
                        EmployeeType.INTERNAL, RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.SATURDAY))
                .thenReturn(List.of());

        CreateCompensationRateRequest request = new CreateCompensationRateRequest(
                EmployeeType.INTERNAL, OvertimeDayType.SATURDAY, "Saturday night", from, to, new BigDecimal("75.00"));

        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should pass validation when same time window and day type exists for a different employee type")
    void shouldPassWhenSameTimeWindowAndDayTypeButDifferentEmployeeType() {
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(22, 0);

        CompensationRate existing = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Evening",
                from,
                to,
                new BigDecimal("35.00"));

        when(compensationRateGateway.findByEmployeeTypeAndRateCategoryAndOvertimeDayType(
                        EmployeeType.EXTERNAL, RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(List.of());

        CreateCompensationRateRequest request = new CreateCompensationRateRequest(
                EmployeeType.EXTERNAL, OvertimeDayType.WEEKDAY, "Evening external", from, to, new BigDecimal("35.00"));

        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }
}
