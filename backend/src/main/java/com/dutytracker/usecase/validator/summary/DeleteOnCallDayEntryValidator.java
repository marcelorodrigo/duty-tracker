package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteOnCallDayEntryValidator implements RequestValidator<DeleteOnCallDayEntryRequest> {

    @Override
    public void validate(DeleteOnCallDayEntryRequest request) {
        // no-op
    }
}
