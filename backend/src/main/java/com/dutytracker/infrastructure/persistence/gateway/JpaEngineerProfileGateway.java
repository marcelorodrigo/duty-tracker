package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.EngineerProfileGateway;
import com.dutytracker.domain.model.EngineerProfile;
import com.dutytracker.infrastructure.persistence.entity.EngineerProfileEntity;
import com.dutytracker.infrastructure.persistence.repository.EngineerProfileJpaRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Optional;
import java.util.Set;

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
                domain.createdAt()
        );
    }

    private EngineerProfile toDomain(EngineerProfileEntity entity) {
        return new EngineerProfile(
                entity.getId(),
                entity.getEmployeeType(),
                entity.getWorkingDays(),
                entity.getWorkStartTime(),
                entity.getWorkEndTime(),
                entity.getCreatedAt()
        );
    }
}
