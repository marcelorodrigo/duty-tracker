package com.dutytracker.usecase.summary;

import com.dutytracker.gateway.OvertimeEntryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;

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
