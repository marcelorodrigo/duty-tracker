package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.model.OnCallDayEntry;
import com.dutytracker.domain.model.StandbyRateType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcOnCallDayEntryGateway implements OnCallDayEntryGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcOnCallDayEntryGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public OnCallDayEntry save(OnCallDayEntry entry) {
        if (entry.id() == null) {
            return insertOne(entry);
        } else {
            String sql = """
                    UPDATE on_call_day_entry SET hours = :hours, rate_type = :rateType,
                    capped = :capped, time_for_time_flag = :timeForTimeFlag,
                    manual_override = :manualOverride WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(entry));
            return findById(entry.id()).orElseThrow();
        }
    }

    @Override
    public List<OnCallDayEntry> saveAll(List<OnCallDayEntry> entries) {
        List<OnCallDayEntry> saved = new ArrayList<>();
        for (OnCallDayEntry entry : entries) {
            saved.add(insertOne(entry));
        }
        return saved;
    }

    private OnCallDayEntry insertOne(OnCallDayEntry entry) {
        String sql = """
                INSERT INTO on_call_day_entry
                (on_call_period_id, date, hours, rate_type, capped, time_for_time_flag, manual_override)
                VALUES (:onCallPeriodId, :date, :hours, :rateType, :capped, :timeForTimeFlag, :manualOverride)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, buildParams(entry), keyHolder, new String[]{"id"});
        Long id = keyHolder.getKey().longValue();
        return new OnCallDayEntry(id, entry.onCallPeriodId(), entry.date(), entry.hours(),
                entry.rateType(), entry.capped(), entry.timeForTimeFlag(), entry.manualOverride());
    }

    @Override
    public List<OnCallDayEntry> findByOnCallPeriodId(Long onCallPeriodId) {
        return jdbc.query(
                "SELECT * FROM on_call_day_entry WHERE on_call_period_id = :id ORDER BY date",
                Map.of("id", onCallPeriodId), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Optional<OnCallDayEntry> findById(Long id) {
        return jdbc.query("SELECT * FROM on_call_day_entry WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM on_call_day_entry WHERE id = :id", Map.of("id", id));
    }

    @Override
    public void deleteByOnCallPeriodId(Long onCallPeriodId) {
        jdbc.update("DELETE FROM on_call_day_entry WHERE on_call_period_id = :id",
                Map.of("id", onCallPeriodId));
    }

    private MapSqlParameterSource buildParams(OnCallDayEntry e) {
        return new MapSqlParameterSource()
                .addValue("id", e.id())
                .addValue("onCallPeriodId", e.onCallPeriodId())
                .addValue("date", e.date())
                .addValue("hours", e.hours())
                .addValue("rateType", e.rateType().name())
                .addValue("capped", e.capped())
                .addValue("timeForTimeFlag", e.timeForTimeFlag())
                .addValue("manualOverride", e.manualOverride());
    }

    private OnCallDayEntry mapRow(ResultSet rs) throws SQLException {
        return new OnCallDayEntry(
                rs.getLong("id"),
                rs.getLong("on_call_period_id"),
                rs.getObject("date", LocalDate.class),
                rs.getBigDecimal("hours"),
                StandbyRateType.valueOf(rs.getString("rate_type")),
                rs.getBoolean("capped"),
                rs.getBoolean("time_for_time_flag"),
                rs.getBoolean("manual_override")
        );
    }
}
