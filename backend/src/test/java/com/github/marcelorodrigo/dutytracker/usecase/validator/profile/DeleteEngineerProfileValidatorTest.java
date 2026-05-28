package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteEngineerProfileValidatorTest {

    private final DeleteEngineerProfileValidator validator = new DeleteEngineerProfileValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new DeleteEngineerProfileRequest();

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
