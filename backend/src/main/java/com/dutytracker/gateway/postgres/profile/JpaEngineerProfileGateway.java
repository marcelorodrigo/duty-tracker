package com.dutytracker.gateway.postgres.profile;

import com.dutytracker.domain.EngineerProfile;
import com.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.gateway.profile.EngineerProfileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaEngineerProfileGateway implements EngineerProfileGateway {

    private final EngineerProfileJpaRepository repository;
    private final EngineerProfileMapper mapper;

    @Override
    public EngineerProfile save(EngineerProfile profile) {
        var entity = mapper.toEntity(profile);
        var saved = repository.save(entity);
        return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<EngineerProfile> find() {
        return repository.findAll().stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public void delete() {
        repository.deleteAll();
    }
}
