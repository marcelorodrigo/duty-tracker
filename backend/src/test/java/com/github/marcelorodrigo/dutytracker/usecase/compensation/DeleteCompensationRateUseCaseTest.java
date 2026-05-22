package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.DeleteCompensationRateValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteCompensationRateUseCaseTest {

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    DeleteCompensationRateValidator validator;

    @InjectMocks
    DeleteCompensationRateUseCase useCase;

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
                .when(validator)
                .validate(request);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ProfileAlreadyExistsException.class)
                .hasMessageContaining("Cannot delete base rate row");
    }
}
