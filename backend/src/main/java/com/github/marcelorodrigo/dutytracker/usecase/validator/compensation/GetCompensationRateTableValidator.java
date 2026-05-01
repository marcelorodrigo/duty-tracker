package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCompensationRateTableValidator implements RequestValidator<GetCompensationRateTableRequest> {

    @Override
    public void validate(GetCompensationRateTableRequest request) {
        // employeeType is nullable — no validation required
    }
}
