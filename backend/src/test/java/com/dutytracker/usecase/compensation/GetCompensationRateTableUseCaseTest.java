package com.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.validator.compensation.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCompensationRateTableUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    GetCompensationRateTableValidator validator;

    @InjectMocks
    GetCompensationRateTableUseCase useCase;

    private static final CompensationRate RATE_BASE = new CompensationRate(
            1L,
            RateCategory.OVERTIME_BASE,
            null,
            "Base",
            LocalTime.of(0, 0),
            LocalTime.of(23, 59),
            BigDecimal.valueOf(125));

    private static final CompensationRate RATE_ALLOWANCE = new CompensationRate(
            2L,
            RateCategory.OVERTIME_ALLOWANCE,
            OvertimeDayType.WEEKDAY,
            "Weekday allowance",
            LocalTime.of(0, 0),
            LocalTime.of(23, 59),
            BigDecimal.valueOf(50));

    @Test
    void returnsAllRates() {
        when(compensationRateGateway.findAll()).thenReturn(List.of(RATE_BASE, RATE_ALLOWANCE));

        var result = useCase.execute(new GetCompensationRateTableRequest());

        assertThat(result.rates()).hasSize(2);
    }

    @Test
    void returnsEmptyListWhenNoRates() {
        when(compensationRateGateway.findAll()).thenReturn(List.of());

        var result = useCase.execute(new GetCompensationRateTableRequest());

        assertThat(result.rates()).isEmpty();
    }
}
