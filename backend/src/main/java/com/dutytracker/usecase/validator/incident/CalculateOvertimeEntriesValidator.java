package com.dutytracker.usecase.validator.incident;

import com.dutytracker.usecase.request.incident.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalculateOvertimeEntriesValidator {
    public void validate(CalculateOvertimeEntriesRequest request) {
        // no-op
    }
}
