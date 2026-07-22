package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.CreateCompensationRateValidator;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Spy
    CompensationRateResponseMapper responseMapper = new CompensationRateResponseMapperImpl();

    @Mock
    CreateCompensationRateValidator validator;

    @InjectMocks
    CreateCompensationRateUseCase useCase;

    private static final CreateCompensationRateRequest VALID_REQUEST = new CreateCompensationRateRequest(
            OvertimeDayType.WEEKDAY, "Night shift", LocalTime.of(22, 0), LocalTime.of(6, 0), BigDecimal.valueOf(150));

    @Test
    @DisplayName("should create a compensation rate when the request is valid")
    void shouldCreateCompensationRateWhenRequestIsValid() {
        // given
        var saved = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                BigDecimal.valueOf(150));
        when(compensationRateGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(result.overtimeDayType()).isEqualTo(OvertimeDayType.WEEKDAY);
        assertThat(result.label()).isEqualTo("Night shift");
    }

    @Test
    @DisplayName("should throw an exception when the compensation rate already exists")
    void shouldThrowExceptionWhenCompensationRateAlreadyExists() {
        // given
        doThrow(new DuplicateCompensationRateException("An OVERTIME_ALLOWANCE rate already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(DuplicateCompensationRateException.class);
    }
}
