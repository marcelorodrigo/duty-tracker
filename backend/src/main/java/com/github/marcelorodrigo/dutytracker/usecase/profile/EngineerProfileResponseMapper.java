package com.github.marcelorodrigo.dutytracker.usecase.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.usecase.mapper.StrictMapperConfig;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;

@Mapper(config = StrictMapperConfig.class)
public interface EngineerProfileResponseMapper {

    EngineerProfileResponse toResponse(EngineerProfile profile);

    default List<String> toWorkingDayNames(Set<DayOfWeek> workingDays) {
        if (workingDays == null) {
            return null;
        }
        return workingDays.stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(DayOfWeek::name)
                .toList();
    }
}
