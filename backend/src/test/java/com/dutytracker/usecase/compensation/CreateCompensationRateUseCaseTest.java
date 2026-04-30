package com.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.dutytracker.usecase.validator.compensation.*;
import java.math.BigDecimal;
import java.time.LocalTime;
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
    void createsRateSuccessfully() {
        var saved = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                BigDecimal.valueOf(150));
        var response = new CompensationRateResponse(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                BigDecimal.valueOf(150));
        when(compensationRateGateway.save(any())).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(response);

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isOne();
        assertThat(result.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(result.overtimeDayType()).isEqualTo(OvertimeDayType.WEEKDAY);
        assertThat(result.label()).isEqualTo("Night shift");
    }

    @Test
    void throwsOnDuplicate() {
        doThrow(new DuplicateCompensationRateException("An OVERTIME_ALLOWANCE rate already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(DuplicateCompensationRateException.class);
    }
}
