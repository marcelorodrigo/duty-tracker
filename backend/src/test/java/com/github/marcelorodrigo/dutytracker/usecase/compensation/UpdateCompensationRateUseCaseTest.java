package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCompensationRateUseCaseTest {

    @Mock
    private CompensationRateGateway compensationRateGateway;

    @Mock
    private UpdateCompensationRateValidator validator;

    @Captor
    private ArgumentCaptor<CompensationRate> rateCaptor;

    @InjectMocks
    private UpdateCompensationRateUseCase useCase;

    @Test
    @DisplayName("should update an existing compensation rate through the update port")
    void shouldUpdateExistingCompensationRateThroughUpdatePort() {
        // given
        var existing = new CompensationRate(
                1L,
                RateCategory.OVERTIME_BASE,
                null,
                "Old label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                BigDecimal.valueOf(100));
        var updated = new CompensationRate(
                1L,
                RateCategory.OVERTIME_BASE,
                null,
                "New label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                BigDecimal.valueOf(130));
        when(compensationRateGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(compensationRateGateway.update(any())).thenReturn(updated);

        // when
        var result = useCase.execute(new UpdateCompensationRateRequest(1L, BigDecimal.valueOf(130), "New label"));

        // then
        assertThat(result.percentage()).isEqualByComparingTo(BigDecimal.valueOf(130));
        assertThat(result.label()).isEqualTo("New label");
        verify(compensationRateGateway).update(rateCaptor.capture());
        assertThat(rateCaptor.getValue()).satisfies(rate -> {
            assertThat(rate.id()).isEqualTo(existing.id());
            assertThat(rate.rateCategory()).isEqualTo(existing.rateCategory());
            assertThat(rate.label()).isEqualTo("New label");
            assertThat(rate.percentage()).isEqualByComparingTo(BigDecimal.valueOf(130));
        });
    }

    @Test
    @DisplayName("should reject update when compensation rate does not exist")
    void shouldRejectUpdateWhenCompensationRateDoesNotExist() {
        // given
        when(compensationRateGateway.findById(999L)).thenReturn(Optional.empty());
        var request = new UpdateCompensationRateRequest(999L, BigDecimal.TEN, "Label");

        // when / then
        assertThatExceptionOfType(CompensationRateNotFoundException.class)
                .isThrownBy(() -> useCase.execute(request))
                .withMessageContaining("999");
        verify(compensationRateGateway, never()).update(any());
    }
}
