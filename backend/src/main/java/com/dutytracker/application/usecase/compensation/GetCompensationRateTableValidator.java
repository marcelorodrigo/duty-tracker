package com.dutytracker.application.usecase.compensation;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetCompensationRateTableValidator implements RequestValidator<GetCompensationRateTableRequest> {

    @Override
    public void validate(GetCompensationRateTableRequest request) {
        // employeeType is nullable — no validation required
    }
}
