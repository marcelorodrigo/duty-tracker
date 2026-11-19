package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListOnCallPeriodsValidatorTest {

    private final ListOnCallPeriodsValidator validator = new ListOnCallPeriodsValidator();

    @Test
    @DisplayName("should accept a list on-call periods request")
    void shouldAcceptListOnCallPeriodsRequest() {
        // given
        var request = new ListOnCallPeriodsRequest(new PaginationRequest(0, 20, List.of()));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
