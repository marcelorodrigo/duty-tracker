package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOnCallPeriodValidator implements RequestValidator<DeleteOnCallPeriodRequest> {

    @Override
    public void validate(DeleteOnCallPeriodRequest request) {
        // no-op: period existence is not a precondition for delete
    }
}
