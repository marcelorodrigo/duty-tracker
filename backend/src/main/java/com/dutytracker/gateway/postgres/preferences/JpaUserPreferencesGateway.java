package com.dutytracker.gateway.postgres.preferences;

import com.dutytracker.domain.UserPreferences;
import com.dutytracker.gateway.postgres.entity.UserPreferencesEntity;
import com.dutytracker.gateway.postgres.repository.UserPreferencesJpaRepository;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.gateway.preferences.UserPreferencesMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaUserPreferencesGateway implements UserPreferencesGateway {

    private final UserPreferencesJpaRepository repository;
    private final UserPreferencesMapper mapper;

    @Override
    public UserPreferences save(UserPreferences preferences) {
        UserPreferencesEntity entity = mapper.toEntity(preferences);
        UserPreferencesEntity saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<UserPreferences> find() {
        return repository.findAll().stream().findFirst().map(mapper::toDomain);
    }
}
