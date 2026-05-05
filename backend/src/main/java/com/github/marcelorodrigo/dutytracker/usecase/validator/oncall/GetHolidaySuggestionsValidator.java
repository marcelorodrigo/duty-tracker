package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetHolidaySuggestionsValidator implements RequestValidator<GetHolidaySuggestionsRequest> {

    @Override
    public void validate(GetHolidaySuggestionsRequest request) {
        if (request.start() == null || request.end() == null) {
            throw new InvalidHolidaySuggestionRangeException("Start and end dates are required");
        }
        if (request.start().isAfter(request.end())) {
            throw new InvalidHolidaySuggestionRangeException("Start date must not be after end date");
        }
    }
}
