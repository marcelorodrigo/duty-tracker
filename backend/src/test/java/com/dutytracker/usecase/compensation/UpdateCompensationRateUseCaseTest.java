package com.dutytracker.usecase.compensation;

import com.dutytracker.gateway.CompensationRateGateway;
import com.dutytracker.domain.model.CompensationRate;
import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.domain.model.RateCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCompensationRateUseCaseTest {

    @Mock CompensationRateGateway compensationRateGateway;
    @Mock UpdateCompensationRateValidator validator;
    @InjectMocks UpdateCompensationRateUseCase useCase;

    @Test
    void updatesRateSuccessfully() {
        CompensationRate existing = new CompensationRate(
                1L, EmployeeType.INTERNAL, RateCategory.OVERTIME_BASE,
                "Old label", LocalTime.of(0, 0), LocalTime.of(23, 59), BigDecimal.valueOf(100));
        CompensationRate updated = new CompensationRate(
                1L, EmployeeType.INTERNAL, RateCategory.OVERTIME_BASE,
                "New label", LocalTime.of(0, 0), LocalTime.of(23, 59), BigDecimal.valueOf(130));
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(compensationRateGateway.update(any())).thenReturn(updated);

        var result = useCase.execute(new UpdateCompensationRateRequest(1L, BigDecimal.valueOf(130), "New label"));

        assertThat(result.percentage()).isEqualByComparingTo(BigDecimal.valueOf(130));
        assertThat(result.label()).isEqualTo("New label");
    }
}
