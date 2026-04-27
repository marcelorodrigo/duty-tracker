package com.dutytracker.application.usecase.summary;

import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteOvertimeEntryUseCaseTest {

    @Mock OvertimeEntryGateway overtimeEntryGateway;
    @Mock DeleteOvertimeEntryValidator validator;

    DeleteOvertimeEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteOvertimeEntryUseCase(overtimeEntryGateway, validator);
    }

    @Test
    @DisplayName("should call deleteById with the given entry id")
    void shouldCallDeleteById() {
        useCase.execute(new DeleteOvertimeEntryRequest(9L));

        verify(overtimeEntryGateway).deleteById(9L);
    }
}
