package com.github.marcelorodrigo.dutytracker.usecase.response.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertimeEntriesResponseTest {

    @Test
    @DisplayName("should create an explicit no-overtime result")
    void shouldCreateExplicitNoOvertimeResult() {
        // given
        var incidentId = 42L;

        // when
        var result = OvertimeEntriesResponse.noOvertime(incidentId);

        // then
        assertThat(result.incidentId()).isEqualTo(incidentId);
        assertThat(result.status()).isEqualTo(OvertimeCalculationStatus.NO_OVERTIME);
        assertThat(result.entries()).isEmpty();
    }

    @Test
    @DisplayName("should reject no-overtime results that contain entries")
    void shouldRejectNoOvertimeResultsThatContainEntries() {
        // given
        var entries = List.of(new OvertimeEntryResponse(42L, null, null, null, null, null, null, false));

        // when / then
        assertThatThrownBy(() -> new OvertimeEntriesResponse(42L, OvertimeCalculationStatus.NO_OVERTIME, entries))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NO_OVERTIME results cannot contain entries");
    }

    @Test
    @DisplayName("should reject calculated results that contain no entries")
    void shouldRejectCalculatedResultsThatContainNoEntries() {
        // given
        var entries = List.<OvertimeEntryResponse>of();

        // when / then
        assertThatThrownBy(
                        () -> new OvertimeEntriesResponse(42L, OvertimeCalculationStatus.OVERTIME_CALCULATED, entries))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OVERTIME_CALCULATED results require entries");
    }
}
