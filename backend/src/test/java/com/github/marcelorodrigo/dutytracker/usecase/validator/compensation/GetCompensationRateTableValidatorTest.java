package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetCompensationRateTableValidatorTest {

    private final GetCompensationRateTableValidator validator = new GetCompensationRateTableValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new GetCompensationRateTableRequest(new PaginationRequest(0, 20, List.of()));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
