package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOvertimeEntryValidator implements RequestValidator<DeleteOvertimeEntryRequest> {

    @Override
    public void validate(DeleteOvertimeEntryRequest request) {
        // no-op
    }
}
