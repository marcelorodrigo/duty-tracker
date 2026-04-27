package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteOvertimeEntryValidator implements RequestValidator<DeleteOvertimeEntryRequest> {

    @Override
    public void validate(DeleteOvertimeEntryRequest request) {
        // no-op
    }
}
