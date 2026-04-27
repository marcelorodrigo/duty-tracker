package com.dutytracker.usecase.summary;

import com.dutytracker.gateway.OnCallDayEntryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteOnCallDayEntryUseCaseTest {

    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock DeleteOnCallDayEntryValidator validator;

    DeleteOnCallDayEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteOnCallDayEntryUseCase(onCallDayEntryGateway, validator);
    }

    @Test
    @DisplayName("should call deleteById with the given entry id")
    void shouldCallDeleteById() {
        useCase.execute(new DeleteOnCallDayEntryRequest(7L));

        verify(onCallDayEntryGateway).deleteById(7L);
    }
}
