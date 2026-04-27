package com.dutytracker.usecase.compensation;







import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.response.compensation.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
