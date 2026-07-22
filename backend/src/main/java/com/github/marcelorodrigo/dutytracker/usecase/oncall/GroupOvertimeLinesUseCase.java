package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GroupOvertimeLinesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupOvertimeLinesUseCase implements UseCase<GroupOvertimeLinesRequest, GroupedOvertimeLinesResponse> {

    private final OvertimeLinesGrouper grouper;

    @Override
    public GroupedOvertimeLinesResponse execute(GroupOvertimeLinesRequest request) {
        return grouper.group(request.entries());
    }
}
