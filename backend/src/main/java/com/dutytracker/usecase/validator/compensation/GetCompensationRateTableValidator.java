package com.dutytracker.usecase.validator.compensation;

import com.dutytracker.usecase.request.compensation.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetCompensationRateTableValidator implements RequestValidator<GetCompensationRateTableRequest> {

    @Override
    public void validate(GetCompensationRateTableRequest request) {
        // employeeType is nullable — no validation required
    }
}
