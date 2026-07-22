package com.github.marcelorodrigo.dutytracker.gateway.postgres.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileAlreadyExistsException;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.ConstraintViolationDetector;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaEngineerProfileGateway implements EngineerProfileGateway {

    private static final String PROFILE_SINGLETON_CONSTRAINT = "uq_engineer_profile_singleton";

    private final EngineerProfileJpaRepository repository;
    private final EngineerProfileMapper mapper;

    @Override
    public EngineerProfile save(EngineerProfile profile) {
        val entity = mapper.toEntity(profile);
        try {
            val saved = repository.saveAndFlush(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationDetector.causedBy(exception, PROFILE_SINGLETON_CONSTRAINT)) {
                throw new ProfileAlreadyExistsException("An engineer profile already exists");
            }
            throw exception;
        }
    }

    @Override
    public Optional<EngineerProfile> find() {
        return repository.findFirstByOrderByIdAsc().map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
