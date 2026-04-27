package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.IncidentGateway;
import com.dutytracker.domain.model.Incident;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcIncidentGateway implements IncidentGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcIncidentGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Incident save(Incident incident) {
        if (incident.id() == null) {
            String sql = """
                    INSERT INTO incident (on_call_period_id, date, start_time, end_time, created_at)
                    VALUES (:onCallPeriodId, :date, :startTime, :endTime, now())
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(sql, buildParams(incident), keyHolder, new String[]{"id"});
            Long id = keyHolder.getKey().longValue();
            return findById(id).orElseThrow();
        } else {
            String sql = """
                    UPDATE incident SET on_call_period_id = :onCallPeriodId, date = :date,
                    start_time = :startTime, end_time = :endTime WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(incident));
            return findById(incident.id()).orElseThrow();
        }
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return jdbc.query("SELECT * FROM incident WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    @Override
    public List<Incident> findByOnCallPeriodId(Long onCallPeriodId) {
        return jdbc.query(
                "SELECT * FROM incident WHERE on_call_period_id = :id ORDER BY date, start_time",
                Map.of("id", onCallPeriodId), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public List<Incident> findAll() {
        return jdbc.query("SELECT * FROM incident ORDER BY date DESC, start_time DESC",
                Map.of(), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM incident WHERE id = :id", Map.of("id", id));
    }

    private MapSqlParameterSource buildParams(Incident i) {
        return new MapSqlParameterSource()
                .addValue("id", i.id())
                .addValue("onCallPeriodId", i.onCallPeriodId())
                .addValue("date", i.date())
                .addValue("startTime", i.startTime())
                .addValue("endTime", i.endTime());
    }

    private Incident mapRow(ResultSet rs) throws SQLException {
        Long onCallPeriodId = rs.getLong("on_call_period_id");
        if (rs.wasNull()) onCallPeriodId = null;
        return new Incident(
                rs.getLong("id"),
                onCallPeriodId,
                rs.getObject("date", LocalDate.class),
                rs.getObject("start_time", LocalTime.class),
                rs.getObject("end_time", LocalTime.class),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
