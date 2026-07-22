package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.CreateCompensationRateValidator;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    CompensationRateResponseMapper responseMapper;

    @Mock
    CreateCompensationRateValidator validator;

    @InjectMocks
    CreateCompensationRateUseCase useCase;

    private static final CreateCompensationRateRequest VALID_REQUEST = new CreateCompensationRateRequest(
            OvertimeDayType.WEEKDAY, "Night shift", LocalTime.of(22, 0), LocalTime.of(6, 0), BigDecimal.valueOf(150));

    @Test
    @DisplayName("should create an overtime allowance rate when it is unique")
    void shouldCreateOvertimeAllowanceRateWhenItIsUnique() {
        // given
        var saved = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                Percentage.of(BigDecimal.valueOf(150)));
        var response = new CompensationRateResponse(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                BigDecimal.valueOf(150));
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(List.of());
        when(compensationRateGateway.save(any())).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(response);

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(result.overtimeDayType()).isEqualTo(OvertimeDayType.WEEKDAY);
        assertThat(result.label()).isEqualTo("Night shift");
        verify(compensationRateGateway)
                .findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY);
        verify(compensationRateGateway).save(any());
    }

    @Test
    @DisplayName("should reject an exact duplicate overtime allowance rate")
    void shouldRejectExactDuplicateOvertimeAllowanceRate() {
        // given
        var existing = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                VALID_REQUEST.overtimeDayType(),
                "Existing night shift",
                VALID_REQUEST.timeFrom(),
                VALID_REQUEST.timeTo(),
                Percentage.of(BigDecimal.valueOf(125)));
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(List.of(existing));

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST))
                .isInstanceOf(DuplicateCompensationRateException.class)
                .hasMessageContaining("overtimeDayType=WEEKDAY")
                .hasMessageContaining("timeFrom=22:00")
                .hasMessageContaining("timeTo=06:00");
        verify(compensationRateGateway)
                .findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY);
        verify(compensationRateGateway, never()).save(any());
    }
}
