package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.OvertimeEntryGateway;
import com.dutytracker.domain.model.OvertimeEntry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcOvertimeEntryGateway implements OvertimeEntryGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcOvertimeEntryGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public OvertimeEntry save(OvertimeEntry entry) {
        return insertOne(entry);
    }

    @Override
    public List<OvertimeEntry> saveAll(List<OvertimeEntry> entries) {
        List<OvertimeEntry> saved = new ArrayList<>();
        for (OvertimeEntry entry : entries) {
            saved.add(insertOne(entry));
        }
        return saved;
    }

    private OvertimeEntry insertOne(OvertimeEntry entry) {
        String sql = """
                INSERT INTO overtime_entry
                (incident_id, overtime_hours, allowance_hours, allowance_percentage,
                 time_from, time_to, is_allowance_entry, manual_override)
                VALUES (:incidentId, :overtimeHours, :allowanceHours, :allowancePercentage,
                        :timeFrom, :timeTo, :isAllowanceEntry, :manualOverride)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, buildParams(entry), keyHolder, new String[]{"id"});
        Long id = keyHolder.getKey().longValue();
        return new OvertimeEntry(id, entry.incidentId(), entry.overtimeHours(), entry.allowanceHours(),
                entry.allowancePercentage(), entry.timeFrom(), entry.timeTo(),
                entry.isAllowanceEntry(), entry.manualOverride());
    }

    @Override
    public List<OvertimeEntry> findByIncidentId(Long incidentId) {
        return jdbc.query("SELECT * FROM overtime_entry WHERE incident_id = :id ORDER BY id",
                Map.of("id", incidentId), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Optional<OvertimeEntry> findById(Long id) {
        return jdbc.query("SELECT * FROM overtime_entry WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM overtime_entry WHERE id = :id", Map.of("id", id));
    }

    @Override
    public void deleteByIncidentId(Long incidentId) {
        jdbc.update("DELETE FROM overtime_entry WHERE incident_id = :id", Map.of("id", incidentId));
    }

    private MapSqlParameterSource buildParams(OvertimeEntry e) {
        return new MapSqlParameterSource()
                .addValue("incidentId", e.incidentId())
                .addValue("overtimeHours", e.overtimeHours())
                .addValue("allowanceHours", e.allowanceHours())
                .addValue("allowancePercentage", e.allowancePercentage())
                .addValue("timeFrom", e.timeFrom())
                .addValue("timeTo", e.timeTo())
                .addValue("isAllowanceEntry", e.isAllowanceEntry())
                .addValue("manualOverride", e.manualOverride());
    }

    private OvertimeEntry mapRow(ResultSet rs) throws SQLException {
        LocalTime timeFrom = rs.getObject("time_from", LocalTime.class);
        LocalTime timeTo = rs.getObject("time_to", LocalTime.class);
        return new OvertimeEntry(
                rs.getLong("id"),
                rs.getLong("incident_id"),
                rs.getBigDecimal("overtime_hours"),
                rs.getBigDecimal("allowance_hours"),
                rs.getBigDecimal("allowance_percentage"),
                timeFrom,
                timeTo,
                rs.getBoolean("is_allowance_entry"),
                rs.getBoolean("manual_override")
        );
    }
}
