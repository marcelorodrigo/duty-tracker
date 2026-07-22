package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.compensation.DeleteCompensationRateValidator;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("should delete overtime allowance rate")
    void shouldDeleteOvertimeAllowanceRate() {
        // given
        var request = new DeleteCompensationRateRequest(1L);

        // when
        useCase.execute(request);

        // then
        verify(compensationRateGateway).deleteById(1L);
    }

    @Test
    @DisplayName("should throw when attempting to delete base row")
    void shouldThrowWhenAttemptingToDeleteBaseRow() {
        // given
        var request = new DeleteCompensationRateRequest(2L);
        org.mockito.Mockito.doThrow(new ProfileAlreadyExistsException(
                        "Cannot delete base rate row: only OVERTIME_ALLOWANCE rows may be deleted"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ProfileAlreadyExistsException.class)
                .hasMessageContaining("Cannot delete base rate row");
    }
}
