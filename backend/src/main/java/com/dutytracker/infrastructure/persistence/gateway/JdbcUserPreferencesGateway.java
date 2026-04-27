package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;
import com.dutytracker.domain.model.UserPreferences;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcUserPreferencesGateway implements UserPreferencesGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserPreferencesGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserPreferences save(UserPreferences prefs) {
        if (prefs.id() == null) {
            String sql = """
                    INSERT INTO user_preferences (color_scheme, onboarding_step)
                    VALUES (:colorScheme, :onboardingStep)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(sql, buildParams(prefs), keyHolder, new String[]{"id"});
            Long id = keyHolder.getKey().longValue();
            return new UserPreferences(id, prefs.colorScheme(), prefs.onboardingStep());
        } else {
            String sql = """
                    UPDATE user_preferences SET color_scheme = :colorScheme,
                    onboarding_step = :onboardingStep WHERE id = :id
                    """;
            jdbc.update(sql, buildParams(prefs));
            return prefs;
        }
    }

    @Override
    public Optional<UserPreferences> find() {
        String sql = "SELECT * FROM user_preferences LIMIT 1";
        return jdbc.query(sql, Map.of(), (rs, rowNum) -> mapRow(rs)).stream().findFirst();
    }

    private MapSqlParameterSource buildParams(UserPreferences p) {
        return new MapSqlParameterSource()
                .addValue("id", p.id())
                .addValue("colorScheme", p.colorScheme().name())
                .addValue("onboardingStep", p.onboardingStep().name());
    }

    private UserPreferences mapRow(ResultSet rs) throws SQLException {
        return new UserPreferences(
                rs.getLong("id"),
                ColorScheme.valueOf(rs.getString("color_scheme")),
                OnboardingStep.valueOf(rs.getString("onboarding_step"))
        );
    }
}
