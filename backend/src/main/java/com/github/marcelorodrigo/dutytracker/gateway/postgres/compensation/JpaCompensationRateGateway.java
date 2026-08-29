package com.github.marcelorodrigo.dutytracker.gateway.postgres.compensation;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.PaginationMapper;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.CompensationRateJpaRepository;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JpaCompensationRateGateway implements CompensationRateGateway {

    private final CompensationRateJpaRepository repository;
    private final CompensationMapper mapper;

    @Override
    public CompensationRate save(CompensationRate rate) {
        var entity = mapper.toEntity(rate);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<CompensationRate> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Page<CompensationRate> findAll(PaginationRequest pagination) {
        return repository.findAll(PaginationMapper.toPageRequest(pagination)).map(mapper::toDomain);
    }

    @Override
    public List<CompensationRate> findByRateCategory(RateCategory rateCategory) {
        return mapper.toDomainList(repository.findByRateCategory(rateCategory));
    }

    @Override
    public List<CompensationRate> findByRateCategoryAndOvertimeDayType(
            RateCategory rateCategory, OvertimeDayType overtimeDayType) {
        return mapper.toDomainList(repository.findByRateCategoryAndOvertimeDayType(rateCategory, overtimeDayType));
    }

    @Override
    public CompensationRate update(CompensationRate rate) {
        var entity = mapper.toEntity(rate);
        var updated = repository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<CompensationRate> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
