package com.github.marcelorodrigo.dutytracker.gateway.postgres.profile;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class JpaEngineerProfileGateway implements EngineerProfileGateway {

    private final EngineerProfileJpaRepository repository;
    private final EngineerProfileMapper mapper;

    @Override
    @Transactional
    public EngineerProfile save(EngineerProfile profile) {
        val entity = profile.id() == null
                ? mapper.toEntity(profile)
                : repository
                        .findById(profile.id())
                        .map(existing -> {
                            existing.updateDetails(
                                    profile.workingDays(),
                                    profile.workStartTime(),
                                    profile.workEndTime(),
                                    profile.hourlyRate(),
                                    profile.standbyWeekdaySaturdayPercentage(),
                                    profile.standbyWeekdaySundayHolidayPercentage());
                            return existing;
                        })
                        .orElseGet(() -> mapper.toEntity(profile));
        val saved = repository.save(entity);
        return mapper.toDomain(saved);
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
