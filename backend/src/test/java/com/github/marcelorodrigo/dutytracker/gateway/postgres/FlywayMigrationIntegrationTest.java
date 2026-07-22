package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

class FlywayMigrationIntegrationTest extends PostgreSqlRepositoryTestSupport {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @DisplayName("should apply every Flyway migration against PostgreSQL")
    void shouldApplyEveryFlywayMigrationAgainstPostgreSql() {
        // given
        var requiredScripts = List.of("V1__create_schema.sql", "V2__seed_data.sql", "V3__add_database_invariants.sql");

        // when
        var appliedScripts = jdbcClient
                .sql("SELECT script FROM flyway_schema_history WHERE success ORDER BY installed_rank")
                .query(String.class)
                .list();

        // then
        assertThat(appliedScripts).containsAll(requiredScripts);
        assertThat(flyway.info().pending()).isEmpty();
    }
}
