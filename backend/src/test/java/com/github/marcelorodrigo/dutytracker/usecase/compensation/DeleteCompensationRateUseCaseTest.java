package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @InjectMocks
    DeleteCompensationRateUseCase useCase;

    @Test
    @DisplayName("should delete an overtime allowance rate")
    void shouldDeleteOvertimeAllowanceRate() {
        // given
        var request = new DeleteCompensationRateRequest(1L);
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(anOvertimeAllowanceRate()));

        // when
        var result = useCase.execute(request);

        // then
        verify(compensationRateGateway).findById(1L);
        verify(compensationRateGateway).deleteById(1L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should reject deleting a protected base rate")
    void shouldRejectDeletingProtectedBaseRate() {
        // given
        var request = new DeleteCompensationRateRequest(2L);
        when(compensationRateGateway.findById(2L)).thenReturn(Optional.of(aBaseRate()));

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ProfileAlreadyExistsException.class)
                .hasMessageContaining("Cannot delete base rate row");
        verify(compensationRateGateway).findById(2L);
        verify(compensationRateGateway, never()).deleteById(2L);
    }

    @Test
    @DisplayName("should preserve delete behavior when a rate is not found")
    void shouldPreserveDeleteBehaviorWhenRateIsNotFound() {
        // given
        var request = new DeleteCompensationRateRequest(99L);
        when(compensationRateGateway.findById(99L)).thenReturn(Optional.empty());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).isNull();
        verify(compensationRateGateway).findById(99L);
        verify(compensationRateGateway).deleteById(99L);
    }

    private CompensationRate anOvertimeAllowanceRate() {
        return new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Test rate",
                LocalTime.of(18, 0),
                LocalTime.of(22, 0),
                Percentage.of(new BigDecimal("35.00")));
    }

    private CompensationRate aBaseRate() {
        return new CompensationRate(
                2L, RateCategory.OVERTIME_BASE, null, "Base rate", null, null, Percentage.of(new BigDecimal("100.00")));
    }
}
