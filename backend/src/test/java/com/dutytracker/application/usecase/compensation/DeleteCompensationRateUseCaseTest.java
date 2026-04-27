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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCompensationRateUseCaseTest {

    @Mock CompensationRateGateway compensationRateGateway;
    @Mock DeleteCompensationRateValidator validator;
    @InjectMocks DeleteCompensationRateUseCase useCase;

    @Test
    void deletesOvertimeAllowanceRate() {
        var result = useCase.execute(new DeleteCompensationRateRequest(1L));

        verify(compensationRateGateway).deleteById(1L);
        assertThat(result).isNull();
    }

    @Test
    void throwsWhenAttemptingToDeleteBaseRow() {
        var request = new DeleteCompensationRateRequest(2L);
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException(
                "Cannot delete base rate row: only OVERTIME_ALLOWANCE rows may be deleted"))
                .when(validator).validate(request);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ProfileAlreadyExistsException.class)
                .hasMessageContaining("Cannot delete base rate row");
    }
}
