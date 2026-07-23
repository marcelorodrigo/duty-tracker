package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateCompensationRateValidatorTest {

    private final CreateCompensationRateValidator validator = new CreateCompensationRateValidator();

    @Test
    @DisplayName("should throw invalid compensation rate exception when overtimeDayType is null")
    void shouldThrowWhenOvertimeDayTypeIsNull() {
        // given
        var request = new CreateCompensationRateRequest(
                null, "Evening", LocalTime.of(18, 0), LocalTime.of(22, 0), new BigDecimal("35.00"));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessageContaining("overtimeDayType");
    }

    @Test
    @DisplayName("should throw invalid compensation rate exception when timeFrom is null")
    void shouldThrowWhenTimeFromIsNull() {
        // given
        var request = new CreateCompensationRateRequest(
                OvertimeDayType.WEEKDAY, "Evening", null, LocalTime.of(22, 0), new BigDecimal("35.00"));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessageContaining("timeFrom");
    }

    @Test
    @DisplayName("should throw invalid compensation rate exception when timeTo is null")
    void shouldThrowWhenTimeToIsNull() {
        // given
        var request = new CreateCompensationRateRequest(
                OvertimeDayType.WEEKDAY, "Evening", LocalTime.of(18, 0), null, new BigDecimal("35.00"));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessageContaining("timeTo");
    }

    @Test
    @DisplayName("should accept a complete overtime allowance schedule")
    void shouldAcceptCompleteOvertimeAllowanceSchedule() {
        // given
        var request = new CreateCompensationRateRequest(
                OvertimeDayType.WEEKDAY, "Evening", LocalTime.of(18, 0), LocalTime.of(22, 0), new BigDecimal("35.00"));

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }
}
