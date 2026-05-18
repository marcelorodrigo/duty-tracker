package com.github.marcelorodrigo.dutytracker.gateway.controllers.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateIncidentBodyTest {

    @Test
    @DisplayName("UpdateIncidentBody record holds all three fields correctly")
    void shouldCreateRecordWithAllFields() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body = new UpdateIncidentBody("Database timeout", start, end);

        assertThat(body.name()).isEqualTo("Database timeout");
        assertThat(body.startDateTime()).isEqualTo(start);
        assertThat(body.endDateTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("UpdateIncidentBody allows null name")
    void shouldAllowNullName() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body = new UpdateIncidentBody(null, start, end);

        assertThat(body.name()).isNull();
        assertThat(body.startDateTime()).isEqualTo(start);
        assertThat(body.endDateTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("UpdateIncidentBody allows null startDateTime")
    void shouldAllowNullStartDateTime() {
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body = new UpdateIncidentBody("Incident A", null, end);

        assertThat(body.name()).isEqualTo("Incident A");
        assertThat(body.startDateTime()).isNull();
        assertThat(body.endDateTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("UpdateIncidentBody allows null endDateTime")
    void shouldAllowNullEndDateTime() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);

        var body = new UpdateIncidentBody("Incident A", start, null);

        assertThat(body.name()).isEqualTo("Incident A");
        assertThat(body.startDateTime()).isEqualTo(start);
        assertThat(body.endDateTime()).isNull();
    }

    @Test
    @DisplayName("UpdateIncidentBody equality is value-based")
    void shouldSupportValueEquality() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body1 = new UpdateIncidentBody("Network issue", start, end);
        var body2 = new UpdateIncidentBody("Network issue", start, end);

        assertThat(body1).isEqualTo(body2);
        assertThat(body1.hashCode()).isEqualTo(body2.hashCode());
    }

    @Test
    @DisplayName("UpdateIncidentBody with different names are not equal")
    void shouldNotBeEqualWhenNamesDiffer() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body1 = new UpdateIncidentBody("Network issue", start, end);
        var body2 = new UpdateIncidentBody("Database issue", start, end);

        assertThat(body1).isNotEqualTo(body2);
    }

    @Test
    @DisplayName("UpdateIncidentBody toString contains field values")
    void shouldHaveDescriptiveToString() {
        var start = LocalDateTime.of(2024, 3, 15, 9, 0);
        var end = LocalDateTime.of(2024, 3, 15, 11, 30);

        var body = new UpdateIncidentBody("Service disruption", start, end);

        assertThat(body.toString()).contains("Service disruption");
    }
}