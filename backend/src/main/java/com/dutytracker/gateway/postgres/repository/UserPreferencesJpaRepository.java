package com.dutytracker.gateway.postgres.repository;


import com.dutytracker.gateway.postgres.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferencesEntity, Long> {
}
