package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.UpdateCompensationRateValidator;
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
                RateCategory.OVERTIME_BASE,
                null,
                "Old label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                BigDecimal.valueOf(100));
        CompensationRate updated = new CompensationRate(
                1L,
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

        var request = new UpdateCompensationRateRequest(999L, BigDecimal.TEN, "Label");
        assertThatExceptionOfType(CompensationRateNotFoundException.class)
                .isThrownBy(() -> useCase.execute(request))
                .withMessageContaining("999");
    }
}
