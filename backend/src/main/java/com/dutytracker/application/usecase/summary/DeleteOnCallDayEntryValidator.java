package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteOnCallDayEntryValidator implements RequestValidator<DeleteOnCallDayEntryRequest> {

    @Override
    public void validate(DeleteOnCallDayEntryRequest request) {
        // no-op
    }
}
