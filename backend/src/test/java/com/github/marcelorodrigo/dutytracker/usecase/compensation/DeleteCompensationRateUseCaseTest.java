package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProtectedCompensationRateException;
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
        var result = useCase.execute(request);

        // then
        verify(compensationRateGateway).deleteById(1L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should reject deletion when compensation rate is protected")
    void shouldRejectDeletionWhenCompensationRateIsProtected() {
        // given
        var request = new DeleteCompensationRateRequest(2L);
        doThrow(new ProtectedCompensationRateException(2L)).when(validator).validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ProtectedCompensationRateException.class)
                .hasMessageContaining("Compensation rate 2 is protected");
    }
}
