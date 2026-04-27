package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOnCallDayEntryValidator implements RequestValidator<DeleteOnCallDayEntryRequest> {

    @Override
    public void validate(DeleteOnCallDayEntryRequest request) {
        // no-op
    }
}
