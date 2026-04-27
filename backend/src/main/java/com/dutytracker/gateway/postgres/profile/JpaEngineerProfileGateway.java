package com.dutytracker.gateway.postgres.profile;

import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import com.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JpaEngineerProfileGateway implements EngineerProfileGateway {

    private final EngineerProfileJpaRepository repository;

    public JpaEngineerProfileGateway(EngineerProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public EngineerProfile save(EngineerProfile profile) {
        EngineerProfileEntity entity = toEntity(profile);
        EngineerProfileEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<EngineerProfile> find() {
        return repository.findAll().stream().findFirst().map(this::toDomain);
    }

    private EngineerProfileEntity toEntity(EngineerProfile domain) {
        return new EngineerProfileEntity(
                domain.id(),
                domain.employeeType(),
                domain.workingDays(),
                domain.workStartTime(),
                domain.workEndTime(),
                domain.createdAt());
    }

    private EngineerProfile toDomain(EngineerProfileEntity entity) {
        return new EngineerProfile(
                entity.getId(),
                entity.getEmployeeType(),
                entity.getWorkingDays(),
                entity.getWorkStartTime(),
                entity.getWorkEndTime(),
                entity.getCreatedAt());
    }
}
