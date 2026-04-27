package com.dutytracker.usecase.summary;





import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class AddOvertimeEntryUseCaseTest {

    @Mock OvertimeEntryGateway overtimeEntryGateway;
    @Mock AddOvertimeEntryValidator validator;

    AddOvertimeEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddOvertimeEntryUseCase(overtimeEntryGateway, validator);
    }

    @Test
    @DisplayName("should add overtime entry with manualOverride set to true")
    void shouldAddEntryWithManualOverrideTrue() {
        AddOvertimeEntryRequest request = new AddOvertimeEntryRequest(
                10L, new BigDecimal("2.0000"), new BigDecimal("2.0000"), new BigDecimal("50.00"),
                LocalTime.of(22, 0), LocalTime.of(23, 0), false);

        when(overtimeEntryGateway.save(any())).thenAnswer(inv -> {
            OvertimeEntry e = inv.getArgument(0);
            return new OvertimeEntry(60L, e.incidentId(), e.overtimeHours(), e.allowanceHours(),
                    e.allowancePercentage(), e.timeFrom(), e.timeTo(), e.isAllowanceEntry(), e.manualOverride());
        });

        OvertimeEntryResponse result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(60L);
        assertThat(result.incidentId()).isEqualTo(10L);
        assertThat(result.manualOverride()).isTrue();
        assertThat(result.isAllowanceEntry()).isFalse();
        assertThat(result.overtimeHours()).isEqualByComparingTo(new BigDecimal("2.0000"));
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when incident is not found")
    void shouldThrowWhenIncidentNotFound() {
        doThrow(new InvalidIncidentException("Incident not found"))
                .when(validator).validate(any());

        assertThatThrownBy(() -> useCase.execute(
                new AddOvertimeEntryRequest(99L, BigDecimal.ONE, null, null,
                        LocalTime.of(22, 0), LocalTime.of(23, 0), false)))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident not found");
    }
}
