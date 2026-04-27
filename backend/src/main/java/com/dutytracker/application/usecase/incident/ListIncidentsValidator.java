package com.dutytracker.application.usecase.incident;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class ListIncidentsValidator implements RequestValidator<ListIncidentsRequest> {

    @Override
    public void validate(ListIncidentsRequest request) {
        // no-op
    }
}
