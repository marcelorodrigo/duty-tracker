package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.DuplicateCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.CreateCompensationRateValidator;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateCompensationRateUseCaseTest {

    @Mock
    private CompensationRateGateway compensationRateGateway;

    @Mock
    private CompensationRateResponseMapper responseMapper;

    @Mock
    private CreateCompensationRateValidator validator;

    @Captor
    private ArgumentCaptor<CompensationRate> rateCaptor;

    @InjectMocks
    private CreateCompensationRateUseCase useCase;

    private static final CreateCompensationRateRequest VALID_REQUEST = new CreateCompensationRateRequest(
            OvertimeDayType.WEEKDAY, "Night shift", LocalTime.of(22, 0), LocalTime.of(6, 0), BigDecimal.valueOf(150));

    @Test
    @DisplayName("should create a new compensation rate without a persistence identifier")
    void shouldCreateNewCompensationRateWithoutPersistenceIdentifier() {
        // given
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
        when(compensationRateGateway.create(any())).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(response);

        // when
        var result = useCase.execute(VALID_REQUEST);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(result.overtimeDayType()).isEqualTo(OvertimeDayType.WEEKDAY);
        assertThat(result.label()).isEqualTo("Night shift");
        verify(compensationRateGateway).create(rateCaptor.capture());
        assertThat(rateCaptor.getValue()).satisfies(rate -> {
            assertThat(rate.id()).isNull();
            assertThat(rate.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        });
    }

    @Test
    @DisplayName("should reject creation when compensation rate is a duplicate")
    void shouldRejectCreationWhenCompensationRateIsDuplicate() {
        // given
        doThrow(new DuplicateCompensationRateException("An OVERTIME_ALLOWANCE rate already exists"))
                .when(validator)
                .validate(VALID_REQUEST);

        // when / then
        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST)).isInstanceOf(DuplicateCompensationRateException.class);
        verifyNoInteractions(compensationRateGateway, responseMapper);
    }
}
