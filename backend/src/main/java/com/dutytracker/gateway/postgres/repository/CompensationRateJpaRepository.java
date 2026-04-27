package com.dutytracker.gateway.postgres.repository;



import com.dutytracker.domain.EmployeeType;
import com.dutytracker.gateway.postgres.entity.CompensationRateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompensationRateJpaRepository extends JpaRepository<CompensationRateEntity, Long> {
    List<CompensationRateEntity> findByEmployeeType(EmployeeType type);
}
