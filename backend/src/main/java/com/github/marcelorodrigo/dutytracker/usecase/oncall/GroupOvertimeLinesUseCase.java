package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GroupOvertimeLinesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupOvertimeLinesUseCase implements UseCase<GroupOvertimeLinesRequest, GroupedOvertimeLinesResponse> {

    @Override
    public GroupedOvertimeLinesResponse execute(GroupOvertimeLinesRequest request) {
        LinkedHashMap<GroupKey, BigDecimal> hours = new LinkedHashMap<>();
        LinkedHashMap<GroupKey, List<Long>> ids = new LinkedHashMap<>();

        for (ReportOvertimeEntryResponse entry : request.entries()) {
            GroupKey key = new GroupKey(entry.date(), entry.isAllowanceEntry(), entry.allowancePercentage());
            BigDecimal entryHours = entry.isAllowanceEntry() ? entry.allowanceHours() : entry.overtimeHours();

            hours.merge(key, entryHours, BigDecimal::add);
            ids.computeIfAbsent(key, k -> new ArrayList<>());
            if (!ids.get(key).contains(entry.incidentId())) {
                ids.get(key).add(entry.incidentId());
            }
        }

        List<GroupedOvertimeEntryResponse> result = hours.entrySet().stream()
                .map(e -> new GroupedOvertimeEntryResponse(
                        e.getKey().date(),
                        e.getKey().isAllowanceEntry(),
                        e.getKey().allowancePercentage(),
                        e.getValue(),
                        List.copyOf(ids.get(e.getKey()))))
                .toList();

        return new GroupedOvertimeLinesResponse(result);
    }

    private record GroupKey(LocalDate date, boolean isAllowanceEntry, BigDecimal allowancePercentage) {}
}
