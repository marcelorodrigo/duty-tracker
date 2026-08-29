package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class GetCompensationRateTableValidatorTest {

    private final GetCompensationRateTableValidator validator = new GetCompensationRateTableValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new GetCompensationRateTableRequest(Pageable.unpaged());

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
