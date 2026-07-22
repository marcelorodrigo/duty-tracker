package com.github.marcelorodrigo.dutytracker.gateway.profile;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.gateway.postgres.entity.EngineerProfileEntity;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EngineerProfileMapper {

    @Mapping(target = "createdAt", ignore = true)
    EngineerProfileEntity toEntity(EngineerProfile domain);

    EngineerProfile toDomain(EngineerProfileEntity entity);

    default BigDecimal map(Money money) {
        return money == null ? null : money.value();
    }

    default Money mapMoney(BigDecimal value) {
        return value == null ? null : Money.of(value);
    }

    default BigDecimal map(Percentage percentage) {
        return percentage == null ? null : percentage.value();
    }

    default Percentage mapPercentage(BigDecimal value) {
        return value == null ? null : Percentage.of(value);
    }
}
