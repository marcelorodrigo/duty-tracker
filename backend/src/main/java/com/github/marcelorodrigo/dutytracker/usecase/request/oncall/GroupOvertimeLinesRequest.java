package com.github.marcelorodrigo.dutytracker.usecase.request.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.util.List;

public record GroupOvertimeLinesRequest(List<ReportOvertimeEntryResponse> entries) {}
