package com.dutytracker.gateway.postgres.oncall;

import com.dutytracker.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.OnCallPeriod;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import com.dutytracker.gateway.postgres.repository.OnCallPeriodJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class JpaOnCallPeriodGateway implements OnCallPeriodGateway {

    private final OnCallPeriodJpaRepository repository;

    public JpaOnCallPeriodGateway(OnCallPeriodJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OnCallPeriod save(OnCallPeriod period) {
        OnCallPeriodEntity entity = toEntity(period);
        OnCallPeriodEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<OnCallPeriod> findAll() {
        return toDomainList(repository.findAll());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private OnCallPeriodEntity toEntity(OnCallPeriod domain) {
        return new OnCallPeriodEntity(
                domain.id(),
                domain.startDateTime(),
                domain.endDateTime(),
                domain.createdAt()
        );
    }

    private OnCallPeriod toDomain(OnCallPeriodEntity entity) {
        return new OnCallPeriod(
                entity.getId(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getCreatedAt()
        );
    }

    private List<OnCallPeriod> toDomainList(List<OnCallPeriodEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
