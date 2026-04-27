package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.model.OnCallPeriod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcOnCallPeriodGateway implements OnCallPeriodGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcOnCallPeriodGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public OnCallPeriod save(OnCallPeriod period) {
        if (period.id() == null) {
            String sql = """
                    INSERT INTO on_call_period (start_date_time, end_date_time, created_at)
                    VALUES (:startDateTime, :endDateTime, now())
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(sql, buildParams(period), keyHolder, new String[]{"id"});
            Long id = keyHolder.getKey().longValue();
            return findById(id).orElseThrow();
        } else {
            String sql = """
                    UPDATE on_call_period SET start_date_time = :startDateTime, end_date_time = :endDateTime
                    WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(period));
            return findById(period.id()).orElseThrow();
        }
    }

    @Override
    public Optional<OnCallPeriod> findById(Long id) {
        return jdbc.query("SELECT * FROM on_call_period WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    @Override
    public List<OnCallPeriod> findAll() {
        return jdbc.query("SELECT * FROM on_call_period ORDER BY start_date_time DESC",
                Map.of(), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM on_call_period WHERE id = :id", Map.of("id", id));
    }

    private MapSqlParameterSource buildParams(OnCallPeriod p) {
        return new MapSqlParameterSource()
                .addValue("id", p.id())
                .addValue("startDateTime", Timestamp.valueOf(p.startDateTime()))
                .addValue("endDateTime", Timestamp.valueOf(p.endDateTime()));
    }

    private OnCallPeriod mapRow(ResultSet rs) throws SQLException {
        return new OnCallPeriod(
                rs.getLong("id"),
                rs.getTimestamp("start_date_time").toLocalDateTime(),
                rs.getTimestamp("end_date_time").toLocalDateTime(),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
