package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.model.HolidayOverride;
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
public class JdbcHolidayOverrideGateway implements HolidayOverrideGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcHolidayOverrideGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public HolidayOverride save(HolidayOverride override) {
        String sql = """
                INSERT INTO holiday_override (on_call_period_id, date)
                VALUES (:onCallPeriodId, :date)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("onCallPeriodId", override.onCallPeriodId())
                .addValue("date", override.date());
        jdbc.update(sql, params, keyHolder, new String[]{"id"});
        Long id = keyHolder.getKey().longValue();
        return new HolidayOverride(id, override.onCallPeriodId(), override.date());
    }

    @Override
    public List<HolidayOverride> findByOnCallPeriodId(Long onCallPeriodId) {
        return jdbc.query("SELECT * FROM holiday_override WHERE on_call_period_id = :id ORDER BY date",
                Map.of("id", onCallPeriodId), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM holiday_override WHERE id = :id", Map.of("id", id));
    }

    @Override
    public Optional<HolidayOverride> findByOnCallPeriodIdAndDate(Long onCallPeriodId, LocalDate date) {
        return jdbc.query(
                "SELECT * FROM holiday_override WHERE on_call_period_id = :pid AND date = :date",
                Map.of("pid", onCallPeriodId, "date", date),
                (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    private HolidayOverride mapRow(ResultSet rs) throws SQLException {
        return new HolidayOverride(
                rs.getLong("id"),
                rs.getLong("on_call_period_id"),
                rs.getObject("date", LocalDate.class)
        );
    }
}
