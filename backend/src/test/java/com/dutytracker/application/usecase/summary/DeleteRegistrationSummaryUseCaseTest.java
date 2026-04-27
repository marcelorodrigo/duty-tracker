package com.dutytracker.application.usecase.summary;

import com.dutytracker.domain.gateway.RegistrationSummaryGateway;
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
