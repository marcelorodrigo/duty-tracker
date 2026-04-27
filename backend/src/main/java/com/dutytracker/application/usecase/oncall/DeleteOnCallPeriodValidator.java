package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOnCallPeriodValidator implements RequestValidator<DeleteOnCallPeriodRequest> {

    @Override
    public void validate(DeleteOnCallPeriodRequest request) {
        // no-op: period existence is not a precondition for delete
    }
}
