package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FlywayMigrationIntegrationTest extends PostgreSqlRepositoryTestSupport {

    private static final String HISTORICAL_SAMPLE_SCHEMA = "migration_historical_sample";
    private static final String CUSTOMIZED_PROFILE_SCHEMA = "migration_customized_profile";

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    @AfterEach
    void dropMigrationTestSchemas() {
        dropSchema(HISTORICAL_SAMPLE_SCHEMA);
        dropSchema(CUSTOMIZED_PROFILE_SCHEMA);
    }

    @Test
    @DisplayName("should apply every Flyway migration against PostgreSQL")
    void shouldApplyEveryFlywayMigrationAgainstPostgreSql() {
        // given
        var requiredScripts =
                List.of("V1__create_schema.sql", "V2__seed_data.sql", "V3__remove_sample_engineer_profile.sql");

        // when
        var appliedScripts = jdbcClient
                .sql("SELECT script FROM flyway_schema_history WHERE success ORDER BY installed_rank")
                .query(String.class)
                .list();

        // then
        assertThat(appliedScripts).containsAll(requiredScripts);
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    @DisplayName("should leave a fresh database without a profile and preserve compensation rates")
    void shouldLeaveFreshDatabaseWithoutProfileAndPreserveCompensationRates() {
        // given
        var expectedCompensationRateCount = 75L;

        // when
        var profileCount = tableRowCount("public", "engineer_profile");
        var compensationRateCount = tableRowCount("public", "compensation_rate");

        // then
        assertThat(profileCount).isZero();
        assertThat(compensationRateCount).isEqualTo(expectedCompensationRateCount);
    }

    @Test
    @DisplayName("should remove the untouched historical sample profile during an upgrade")
    void shouldRemoveUntouchedHistoricalSampleProfileDuringUpgrade() {
        // given
        migrateToVersionTwo(HISTORICAL_SAMPLE_SCHEMA);
        var profileCountBeforeUpgrade = tableRowCount(HISTORICAL_SAMPLE_SCHEMA, "engineer_profile");

        // when
        migrateToLatest(HISTORICAL_SAMPLE_SCHEMA);

        // then
        assertThat(profileCountBeforeUpgrade).isOne();
        assertThat(tableRowCount(HISTORICAL_SAMPLE_SCHEMA, "engineer_profile")).isZero();
        assertThat(tableRowCount(HISTORICAL_SAMPLE_SCHEMA, "compensation_rate")).isEqualTo(75L);
    }

    @Test
    @DisplayName("should preserve a customized profile during an upgrade")
    void shouldPreserveCustomizedProfileDuringUpgrade() {
        // given
        migrateToVersionTwo(CUSTOMIZED_PROFILE_SCHEMA);
        jdbcClient
                .sql("UPDATE " + CUSTOMIZED_PROFILE_SCHEMA + ".engineer_profile SET hourly_rate = 50.00 WHERE id = 1")
                .update();

        // when
        migrateToLatest(CUSTOMIZED_PROFILE_SCHEMA);

        // then
        assertThat(tableRowCount(CUSTOMIZED_PROFILE_SCHEMA, "engineer_profile")).isOne();
        assertThat(jdbcClient
                        .sql("SELECT hourly_rate FROM " + CUSTOMIZED_PROFILE_SCHEMA + ".engineer_profile WHERE id = 1")
                        .query(BigDecimal.class)
                        .single())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(tableRowCount(CUSTOMIZED_PROFILE_SCHEMA, "compensation_rate"))
                .isEqualTo(75L);
    }

    private void migrateToVersionTwo(String schema) {
        configureFlyway(schema).target("2").load().migrate();
    }

    private void migrateToLatest(String schema) {
        configureFlyway(schema).load().migrate();
    }

    private FluentConfiguration configureFlyway(String schema) {
        return Flyway.configure()
                .dataSource(dataSource)
                .defaultSchema(schema)
                .schemas(schema)
                .locations("classpath:db/migration");
    }

    private long tableRowCount(String schema, String table) {
        return jdbcClient
                .sql("SELECT COUNT(*) FROM " + schema + "." + table)
                .query(Long.class)
                .single();
    }

    private void dropSchema(String schema) {
        jdbcClient.sql("DROP SCHEMA IF EXISTS " + schema + " CASCADE").update();
    }
}
