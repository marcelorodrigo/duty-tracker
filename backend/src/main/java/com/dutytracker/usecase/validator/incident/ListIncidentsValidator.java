package com.dutytracker.usecase.validator.incident;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.incident.*;
import org.springframework.stereotype.Component;

@Component
public class ListIncidentsValidator implements RequestValidator<ListIncidentsRequest> {

    @Override
    public void validate(ListIncidentsRequest request) {
        // no-op
    }
}
