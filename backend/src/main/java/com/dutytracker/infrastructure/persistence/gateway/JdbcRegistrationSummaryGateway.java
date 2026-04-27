package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.RegistrationSummaryGateway;
import com.dutytracker.domain.model.RegistrationSummary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcRegistrationSummaryGateway implements RegistrationSummaryGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcRegistrationSummaryGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public RegistrationSummary save(RegistrationSummary summary) {
        if (summary.id() == null) {
            String sql = """
                    INSERT INTO registration_summary (label, period_start, period_end, created_at, updated_at)
                    VALUES (:label, :periodStart, :periodEnd, now(), now())
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(sql, buildParams(summary), keyHolder, new String[]{"id"});
            Long id = keyHolder.getKey().longValue();
            return findById(id).orElseThrow();
        } else {
            String sql = """
                    UPDATE registration_summary SET label = :label, updated_at = now() WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(summary));
            return findById(summary.id()).orElseThrow();
        }
    }

    @Override
    public Optional<RegistrationSummary> findById(Long id) {
        return jdbc.query("SELECT * FROM registration_summary WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    @Override
    public List<RegistrationSummary> findAll() {
        return jdbc.query("SELECT * FROM registration_summary ORDER BY created_at DESC",
                Map.of(), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM registration_summary WHERE id = :id", Map.of("id", id));
    }

    @Override
    public boolean existsAny() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration_summary", Map.of(), Integer.class);
        return count != null && count > 0;
    }

    private MapSqlParameterSource buildParams(RegistrationSummary s) {
        return new MapSqlParameterSource()
                .addValue("id", s.id())
                .addValue("label", s.label())
                .addValue("periodStart", s.periodStart())
                .addValue("periodEnd", s.periodEnd());
    }

    private RegistrationSummary mapRow(ResultSet rs) throws SQLException {
        return new RegistrationSummary(
                rs.getLong("id"),
                rs.getString("label"),
                rs.getObject("period_start", LocalDate.class),
                rs.getObject("period_end", LocalDate.class),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
