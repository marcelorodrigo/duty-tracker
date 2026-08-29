package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.GetCompensationRateTableValidator;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        when(compensationRateGateway.findAll(any(PaginationRequest.class)))
                .thenReturn(new PageImpl<>(List.of(RATE_BASE, RATE_ALLOWANCE), PageRequest.of(0, 20), 2L));

        var result = useCase.execute(new GetCompensationRateTableRequest(new PaginationRequest(0, 20, List.of())));

        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isZero();
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    void returnsEmptyPageWhenNoRates() {
        when(compensationRateGateway.findAll(any(PaginationRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0L));

        var result = useCase.execute(new GetCompensationRateTableRequest(new PaginationRequest(0, 20, List.of())));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
