package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteCompensationRateValidatorTest {

    private final DeleteCompensationRateValidator validator = new DeleteCompensationRateValidator();

    @Test
    @DisplayName("should accept a delete request")
    void shouldAcceptDeleteRequest() {
        // given
        var request = new DeleteCompensationRateRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
