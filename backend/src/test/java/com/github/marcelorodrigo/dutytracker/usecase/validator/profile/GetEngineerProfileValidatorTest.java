package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetEngineerProfileValidatorTest {

    private final GetEngineerProfileValidator validator = new GetEngineerProfileValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new GetEngineerProfileRequest();

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
