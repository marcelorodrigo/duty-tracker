package com.dutytracker.gateway.postgres.repository;



import com.dutytracker.gateway.postgres.entity.OnCallDayEntryEntity;
import com.dutytracker.gateway.postgres.entity.OnCallPeriodEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
public interface OnCallDayEntryJpaRepository extends JpaRepository<OnCallDayEntryEntity, Long> {
    List<OnCallDayEntryEntity> findByOnCallPeriodId(Long id);

    @Transactional
    void deleteByOnCallPeriod(OnCallPeriodEntity p);
}
