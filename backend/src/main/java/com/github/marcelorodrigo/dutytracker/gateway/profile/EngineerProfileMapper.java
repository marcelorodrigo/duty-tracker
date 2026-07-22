package com.github.marcelorodrigo.dutytracker.gateway.profile;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EngineerProfileMapper {

    default EngineerProfileEntity toEntity(EngineerProfile domain) {
        return new EngineerProfileEntity(
                domain.id(),
                domain.workingDays(),
                domain.workStartTime(),
                domain.workEndTime(),
                domain.hourlyRate(),
                domain.standbyWeekdaySaturdayPercentage(),
                domain.standbyWeekdaySundayHolidayPercentage());
    }

    EngineerProfile toDomain(EngineerProfileEntity entity);
}
