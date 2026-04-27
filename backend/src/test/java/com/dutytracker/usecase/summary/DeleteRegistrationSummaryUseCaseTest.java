package com.dutytracker.usecase.summary;



import com.dutytracker.gateway.summary.RegistrationSummaryGateway;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.summary.*;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class DeleteRegistrationSummaryUseCaseTest {

    @Mock RegistrationSummaryGateway registrationSummaryGateway;
    @Mock DeleteRegistrationSummaryValidator validator;

    DeleteRegistrationSummaryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteRegistrationSummaryUseCase(registrationSummaryGateway, validator);
    }

    @Test
    @DisplayName("should call deleteById with the given summary id")
    void shouldCallDeleteById() {
        useCase.execute(new DeleteRegistrationSummaryRequest(5L));

        verify(registrationSummaryGateway).deleteById(5L);
    }
}
