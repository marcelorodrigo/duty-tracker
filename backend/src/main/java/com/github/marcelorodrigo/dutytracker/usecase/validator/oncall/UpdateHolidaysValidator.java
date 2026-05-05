package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class UpdateHolidaysValidator implements RequestValidator<UpdateHolidaysRequest> {

    @Override
    public void validate(UpdateHolidaysRequest request) {
        // period existence is checked in the use case
    }
}
