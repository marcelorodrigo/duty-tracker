package com.github.marcelorodrigo.dutytracker.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HolidayNotFoundExceptionTest {

    @Test
    @DisplayName("should store the message provided at construction")
    void shouldStoreTheMessageProvidedAtConstruction() {
        // given
        var message = "Holiday with id 42 not found";

        // when
        var exception = new HolidayNotFoundException(message);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}
