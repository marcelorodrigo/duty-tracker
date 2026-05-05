package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.util.List;

public record UpdateHolidaysRequest(Long periodId, List<HolidayResponse> holidays) {}
