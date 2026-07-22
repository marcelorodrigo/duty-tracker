package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.Hours;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OvertimeLinesGrouper {

    public GroupedOvertimeLinesResponse group(List<ReportOvertimeEntryResponse> entries) {
        LinkedHashMap<GroupKey, Hours> hours = new LinkedHashMap<>();
        LinkedHashMap<GroupKey, List<Long>> ids = new LinkedHashMap<>();

        for (ReportOvertimeEntryResponse entry : entries) {
            Percentage allowancePercentage =
                    entry.allowancePercentage() == null ? null : Percentage.of(entry.allowancePercentage());
            GroupKey key = new GroupKey(entry.date(), entry.isAllowanceEntry(), allowancePercentage);
            Hours entryHours = new Hours(entry.isAllowanceEntry() ? entry.allowanceHours() : entry.overtimeHours());

            hours.merge(key, entryHours, Hours::add);
            ids.computeIfAbsent(key, ignored -> new ArrayList<>());
            if (!ids.get(key).contains(entry.incidentId())) {
                ids.get(key).add(entry.incidentId());
            }
        }

        List<GroupedOvertimeEntryResponse> result = hours.entrySet().stream()
                .map(entry -> new GroupedOvertimeEntryResponse(
                        entry.getKey().date(),
                        entry.getKey().isAllowanceEntry(),
                        entry.getKey().allowancePercentage() == null
                                ? null
                                : entry.getKey().allowancePercentage().value(),
                        entry.getValue().value(),
                        List.copyOf(ids.get(entry.getKey()))))
                .toList();

        return new GroupedOvertimeLinesResponse(result);
    }

    private record GroupKey(LocalDate date, boolean isAllowanceEntry, Percentage allowancePercentage) {}
}
