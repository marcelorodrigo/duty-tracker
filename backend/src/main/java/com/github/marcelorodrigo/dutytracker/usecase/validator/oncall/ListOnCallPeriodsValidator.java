package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class ListOnCallPeriodsValidator implements RequestValidator<ListOnCallPeriodsRequest> {

    @Override
    public void validate(ListOnCallPeriodsRequest request) {
        // no-op
    }
}
