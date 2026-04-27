package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class ListOnCallPeriodsValidator implements RequestValidator<ListOnCallPeriodsRequest> {

    @Override
    public void validate(ListOnCallPeriodsRequest request) {
        // no-op
    }
}
