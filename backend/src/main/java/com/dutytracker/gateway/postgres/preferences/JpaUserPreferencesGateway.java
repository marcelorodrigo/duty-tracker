package com.dutytracker.gateway.postgres.preferences;

import com.dutytracker.gateway.UserPreferencesGateway;
import com.dutytracker.domain.UserPreferences;
import com.dutytracker.gateway.postgres.entity.UserPreferencesEntity;
import com.dutytracker.gateway.postgres.repository.UserPreferencesJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class JpaUserPreferencesGateway implements UserPreferencesGateway {

    private final UserPreferencesJpaRepository repository;

    public JpaUserPreferencesGateway(UserPreferencesJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserPreferences save(UserPreferences preferences) {
        UserPreferencesEntity entity = toEntity(preferences);
        UserPreferencesEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<UserPreferences> find() {
        return repository.findAll().stream().findFirst().map(this::toDomain);
    }

    private UserPreferencesEntity toEntity(UserPreferences domain) {
        return new UserPreferencesEntity(
                domain.id(),
                domain.colorScheme(),
                domain.onboardingStep()
        );
    }

    private UserPreferences toDomain(UserPreferencesEntity entity) {
        return new UserPreferences(
                entity.getId(),
                entity.getColorScheme(),
                entity.getOnboardingStep()
        );
    }
}
