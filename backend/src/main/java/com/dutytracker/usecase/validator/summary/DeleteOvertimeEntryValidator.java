package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOvertimeEntryValidator implements RequestValidator<DeleteOvertimeEntryRequest> {

    @Override
    public void validate(DeleteOvertimeEntryRequest request) {
        // no-op
    }
}
