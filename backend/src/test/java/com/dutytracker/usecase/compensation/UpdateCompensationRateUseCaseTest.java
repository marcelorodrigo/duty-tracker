package com.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    UpdateCompensationRateValidator validator;

    @InjectMocks
    UpdateCompensationRateUseCase useCase;

    @Test
    void updatesRateSuccessfully() {
        CompensationRate existing = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_BASE,
                null,
                "Old label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                BigDecimal.valueOf(100));
        CompensationRate updated = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_BASE,
                null,
                "New label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                BigDecimal.valueOf(130));
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(compensationRateGateway.update(any())).thenReturn(updated);

        var result = useCase.execute(new UpdateCompensationRateRequest(1L, BigDecimal.valueOf(130), "New label"));

        assertThat(result.percentage()).isEqualByComparingTo(BigDecimal.valueOf(130));
        assertThat(result.label()).isEqualTo("New label");
    }

    @Test
    void throwsCompensationRateNotFoundWhenRateDoesNotExist() {
        when(compensationRateGateway.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateCompensationRateRequest(999L, BigDecimal.TEN, "Label")))
                .isInstanceOf(CompensationRateNotFoundException.class)
                .hasMessageContaining("999");
    }
}
