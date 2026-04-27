package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.CompensationRateGateway;
import com.dutytracker.domain.model.CompensationRate;
import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.domain.model.RateCategory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcCompensationRateGateway implements CompensationRateGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCompensationRateGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CompensationRate> saveAll(List<CompensationRate> rates) {
        return rates.stream().map(this::insertOne).toList();
    }

    private CompensationRate insertOne(CompensationRate rate) {
        String sql = """
                INSERT INTO compensation_rate (employee_type, rate_category, label, time_from, time_to, percentage)
                VALUES (:employeeType, :rateCategory, :label, :timeFrom, :timeTo, :percentage)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, buildParams(rate), keyHolder, new String[]{"id"});
        Long id = keyHolder.getKey().longValue();
        return new CompensationRate(id, rate.employeeType(), rate.rateCategory(), rate.label(),
                rate.timeFrom(), rate.timeTo(), rate.percentage());
    }

    @Override
    public List<CompensationRate> findAll() {
        return jdbc.query("SELECT * FROM compensation_rate ORDER BY id", Map.of(),
                (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public List<CompensationRate> findByEmployeeType(EmployeeType employeeType) {
        return jdbc.query("SELECT * FROM compensation_rate WHERE employee_type = :type ORDER BY id",
                Map.of("type", employeeType.name()), (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public CompensationRate update(CompensationRate rate) {
        String sql = """
                UPDATE compensation_rate SET label = :label, percentage = :percentage WHERE id = :id
                """;
        jdbc.update(sql, buildParams(rate));
        return rate;
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM compensation_rate WHERE id = :id", Map.of("id", id));
    }

    @Override
    public Optional<CompensationRate> findById(Long id) {
        return jdbc.query("SELECT * FROM compensation_rate WHERE id = :id",
                Map.of("id", id), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    private MapSqlParameterSource buildParams(CompensationRate r) {
        return new MapSqlParameterSource()
                .addValue("id", r.id())
                .addValue("employeeType", r.employeeType().name())
                .addValue("rateCategory", r.rateCategory().name())
                .addValue("label", r.label())
                .addValue("timeFrom", r.timeFrom())
                .addValue("timeTo", r.timeTo())
                .addValue("percentage", r.percentage());
    }

    private CompensationRate mapRow(ResultSet rs) throws SQLException {
        LocalTime timeFrom = rs.getObject("time_from", LocalTime.class);
        LocalTime timeTo = rs.getObject("time_to", LocalTime.class);
        return new CompensationRate(
                rs.getLong("id"),
                EmployeeType.valueOf(rs.getString("employee_type")),
                RateCategory.valueOf(rs.getString("rate_category")),
                rs.getString("label"),
                timeFrom,
                timeTo,
                rs.getBigDecimal("percentage")
        );
    }
}
