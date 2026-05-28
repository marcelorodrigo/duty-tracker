package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteCompensationRateValidatorTest {

    @Mock
    private CompensationRateGateway compensationRateGateway;

    @InjectMocks
    private DeleteCompensationRateValidator validator;

    private static CompensationRate aRate(RateCategory category) {
        return new CompensationRate(
                1L,
                category,
                OvertimeDayType.WEEKDAY,
                "Test label",
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("should pass validation when rate category is OVERTIME_ALLOWANCE")
    void shouldPassValidationWhenRateCategoryIsOvertimeAllowance() {
        // given
        var request = new DeleteCompensationRateRequest(1L);
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(aRate(RateCategory.OVERTIME_ALLOWANCE)));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should pass validation when rate is not found")
    void shouldPassValidationWhenRateIsNotFound() {
        // given
        var request = new DeleteCompensationRateRequest(99L);
        when(compensationRateGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("should throw ProfileAlreadyExistsException when rate category is not OVERTIME_ALLOWANCE")
    @EnumSource(
            value = RateCategory.class,
            names = {"ONCALL_WEEKDAY_SATURDAY", "ONCALL_SUNDAY_HOLIDAY", "OVERTIME_BASE"})
    void shouldThrowExceptionWhenRateCategoryIsNotOvertimeAllowance(RateCategory category) {
        // given
        var request = new DeleteCompensationRateRequest(1L);
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(aRate(category)));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(ProfileAlreadyExistsException.class)
                .hasMessageContaining("Cannot delete base rate row");
    }
}
