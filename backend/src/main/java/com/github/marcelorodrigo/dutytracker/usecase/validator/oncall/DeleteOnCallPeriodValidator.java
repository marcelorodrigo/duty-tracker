package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.DeleteOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteOnCallPeriodValidator implements RequestValidator<DeleteOnCallPeriodRequest> {

    @Override
    public void validate(DeleteOnCallPeriodRequest request) {
        // no-op: period existence is not a precondition for delete
    }
}
