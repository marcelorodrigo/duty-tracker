package com.dutytracker.application.usecase.compensation;

import com.dutytracker.domain.exception.ProfileAlreadyExistsException;
import com.dutytracker.domain.gateway.CompensationRateGateway;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCompensationRateUseCaseTest {

    @Mock CompensationRateGateway compensationRateGateway;
    @Mock CreateCompensationRateValidator validator;
    @InjectMocks CreateCompensationRateUseCase useCase;

    private static final CreateCompensationRateRequest VALID_REQUEST = new CreateCompensationRateRequest(
            EmployeeType.INTERNAL,
            "Night shift",
            LocalTime.of(22, 0),
            LocalTime.of(6, 0),
            BigDecimal.valueOf(150)
    );

    @Test
    void createsRateSuccessfully() {
        CompensationRate saved = new CompensationRate(
                1L, EmployeeType.INTERNAL, RateCategory.OVERTIME_ALLOWANCE,
                "Night shift", LocalTime.of(22, 0), LocalTime.of(6, 0), BigDecimal.valueOf(150));
        when(compensationRateGateway.saveAll(anyList())).thenReturn(List.of(saved));

        var result = useCase.execute(VALID_REQUEST);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(result.label()).isEqualTo("Night shift");
    }

    @Test
    void throwsOnDuplicate() {
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException("An OVERTIME_ALLOWANCE rate already exists"))
                .when(validator).validate(VALID_REQUEST);

        assertThatThrownBy(() -> useCase.execute(VALID_REQUEST))
                .isInstanceOf(ProfileAlreadyExistsException.class);
    }
}
