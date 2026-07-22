package com.github.marcelorodrigo.dutytracker.gateway.postgres.oncall;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.ConstraintViolationDetector;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaOnCallPeriodGateway implements OnCallPeriodGateway {

    private static final String PERIOD_OVERLAP_CONSTRAINT = "ex_on_call_period_no_overlap";

    private final OnCallPeriodJpaRepository repository;
    private final OnCallPeriodMapper mapper;

    @Override
    public OnCallPeriod save(OnCallPeriod period) {
        var entity = mapper.toEntity(period);
        try {
            var saved = repository.saveAndFlush(entity);
            return mapper.toDomain(repository.findById(saved.getId()).orElseThrow());
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationDetector.causedBy(exception, PERIOD_OVERLAP_CONSTRAINT)) {
                throw new OnCallPeriodOverlapException();
            }
            throw exception;
        }
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<OnCallPeriod> findAll() {
        return mapper.toDomainList(repository.findAll(Sort.by("startDateTime").descending()));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsOverlapping(LocalDateTime start, LocalDateTime end, Long excludeId) {
        return repository.existsOverlapping(start, end, excludeId);
    }
}
