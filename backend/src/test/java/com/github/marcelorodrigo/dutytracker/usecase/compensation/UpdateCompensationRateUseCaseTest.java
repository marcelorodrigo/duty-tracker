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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    UpdateCompensationRateValidator validator;

    @Spy
    CompensationRateResponseMapper responseMapper = new CompensationRateResponseMapperImpl();

    @InjectMocks
    UpdateCompensationRateUseCase useCase;

    @Test
    @DisplayName("should update a compensation rate when it exists")
    void shouldUpdateCompensationRateWhenItExists() {
        // given
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

        // when
        var result = useCase.execute(new UpdateCompensationRateRequest(1L, BigDecimal.valueOf(130), "New label"));

        // then
        assertThat(result.percentage()).isEqualByComparingTo(BigDecimal.valueOf(130));
        assertThat(result.label()).isEqualTo("New label");
    }

    @Test
    @DisplayName("should throw an exception when the compensation rate does not exist")
    void shouldThrowExceptionWhenCompensationRateDoesNotExist() {
        // given
        when(compensationRateGateway.findById(999L)).thenReturn(Optional.empty());
        var request = new UpdateCompensationRateRequest(999L, BigDecimal.TEN, "Label");

        // when / then
        assertThatExceptionOfType(CompensationRateNotFoundException.class)
                .isThrownBy(() -> useCase.execute(request))
                .withMessageContaining("999");
    }
}
