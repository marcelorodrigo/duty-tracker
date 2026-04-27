package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class ListOnCallPeriodsValidator implements RequestValidator<ListOnCallPeriodsRequest> {

    @Override
    public void validate(ListOnCallPeriodsRequest request) {
        // no-op
    }
}
