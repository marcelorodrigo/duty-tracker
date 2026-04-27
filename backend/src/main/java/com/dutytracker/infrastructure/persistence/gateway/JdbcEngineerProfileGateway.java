package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.EngineerProfileGateway;
import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.domain.model.EngineerProfile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JdbcEngineerProfileGateway implements EngineerProfileGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcEngineerProfileGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public EngineerProfile save(EngineerProfile profile) {
        if (profile.id() == null) {
            String sql = """
                    INSERT INTO engineer_profile (employee_type, working_days, work_start_time, work_end_time, created_at)
                    VALUES (:employeeType, :workingDays, :workStartTime, :workEndTime, :createdAt)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(sql, buildParams(profile), keyHolder, new String[]{"id"});
            Long id = keyHolder.getKey().longValue();
            return new EngineerProfile(id, profile.employeeType(), profile.workingDays(),
                    profile.workStartTime(), profile.workEndTime(), profile.createdAt());
        } else {
            String sql = """
                    UPDATE engineer_profile SET employee_type = :employeeType, working_days = :workingDays,
                    work_start_time = :workStartTime, work_end_time = :workEndTime WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(profile));
            return profile;
        }
    }

    @Override
    public Optional<EngineerProfile> find() {
        String sql = "SELECT * FROM engineer_profile LIMIT 1";
        return jdbc.query(sql, Map.of(), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    private MapSqlParameterSource buildParams(EngineerProfile p) {
        String days = p.workingDays().stream().map(DayOfWeek::name).collect(Collectors.joining(","));
        return new MapSqlParameterSource()
                .addValue("id", p.id())
                .addValue("employeeType", p.employeeType().name())
                .addValue("workingDays", days)
                .addValue("workStartTime", p.workStartTime())
                .addValue("workEndTime", p.workEndTime())
                .addValue("createdAt", p.createdAt() != null ? p.createdAt().toString() : Instant.now().toString());
    }

    private EngineerProfile mapRow(ResultSet rs) throws SQLException {
        Set<DayOfWeek> days = Arrays.stream(rs.getString("working_days").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new EngineerProfile(
                rs.getLong("id"),
                EmployeeType.valueOf(rs.getString("employee_type")),
                days,
                rs.getObject("work_start_time", LocalTime.class),
                rs.getObject("work_end_time", LocalTime.class),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
