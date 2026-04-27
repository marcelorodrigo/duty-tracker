package com.dutytracker.gateway.preferences;

import com.dutytracker.domain.UserPreferences;
import com.dutytracker.gateway.postgres.entity.UserPreferencesEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPreferencesMapper {

    UserPreferencesEntity toEntity(UserPreferences domain);

    UserPreferences toDomain(UserPreferencesEntity entity);
}
