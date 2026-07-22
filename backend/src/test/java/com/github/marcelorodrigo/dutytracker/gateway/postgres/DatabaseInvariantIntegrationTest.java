package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DatabaseInvariantIntegrationTest extends PostgreSqlRepositoryTestSupport {

    private static final String PROFILE_CONSTRAINT = "uq_engineer_profile_singleton";
    private static final String PERIOD_CONSTRAINT = "ex_on_call_period_no_overlap";
    private static final String INCIDENT_CONSTRAINT = "ex_incident_no_overlap";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void clearInvariantTables() {
        clearPersistedData();
    }

    @AfterEach
    void restoreSeedProfile() {
        clearPersistedData();
        jdbcClient.sql("""
                        INSERT INTO engineer_profile (
                            working_days,
                            work_start_time,
                            work_end_time,
                            hourly_rate,
                            standby_weekday_saturday_pct,
                            standby_sunday_holiday_pct
                        ) VALUES (
                            'FRIDAY,MONDAY,THURSDAY,TUESDAY,WEDNESDAY',
                            '09:00',
                            '17:00',
                            1.00,
                            0.06700,
                            0.08400
                        )
                        """).update();
    }

    @Test
    @DisplayName("should reject a concurrently inserted duplicate engineer profile")
    void shouldRejectAConcurrentlyInsertedDuplicateEngineerProfile() throws Exception {
        // given
        var insert = """
                INSERT INTO engineer_profile (
                    working_days,
                    work_start_time,
                    work_end_time,
                    hourly_rate,
                    standby_weekday_saturday_pct,
                    standby_sunday_holiday_pct
                ) VALUES (
                    'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',
                    '09:00',
                    '17:00',
                    50.00,
                    0.06700,
                    0.08400
                )
                """;

        // when
        var results = insertConcurrently(insert, PreparedStatement::clearParameters);

        // then
        assertOneAcceptedAndOneRejected(results, "23505", PROFILE_CONSTRAINT);
    }

    @Test
    @DisplayName("should reject concurrently inserted overlapping on-call periods")
    void shouldRejectConcurrentlyInsertedOverlappingOnCallPeriods() throws Exception {
        // given
        var insert = "INSERT INTO on_call_period (start_date_time, end_date_time) VALUES (?, ?)";
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var end = start.plusHours(8);

        // when
        var results = insertConcurrently(insert, statement -> {
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
        });

        // then
        assertOneAcceptedAndOneRejected(results, "23P01", PERIOD_CONSTRAINT);
    }

    @Test
    @DisplayName("should reject concurrently inserted overlapping incidents in one on-call period")
    void shouldRejectConcurrentlyInsertedOverlappingIncidentsInOneOnCallPeriod() throws Exception {
        // given
        var periodId = jdbcClient.sql("""
                        INSERT INTO on_call_period (start_date_time, end_date_time)
                        VALUES ('2026-07-22 00:00', '2026-07-23 00:00')
                        RETURNING id
                        """).query(Long.class).single();
        var insert = """
                INSERT INTO incident (
                    on_call_period_id,
                    name,
                    start_date_time,
                    end_date_time
                ) VALUES (?, 'Production incident', ?, ?)
                """;
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var end = start.plusHours(1);

        // when
        var results = insertConcurrently(insert, statement -> {
            statement.setLong(1, periodId);
            statement.setTimestamp(2, Timestamp.valueOf(start));
            statement.setTimestamp(3, Timestamp.valueOf(end));
        });

        // then
        assertOneAcceptedAndOneRejected(results, "23P01", INCIDENT_CONSTRAINT);
    }

    private List<InsertResult> insertConcurrently(String insert, StatementBinder binder) throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> insertInTransaction(insert, binder, ready, start));
            var second = executor.submit(() -> insertInTransaction(insert, binder, ready, start));
            try {
                assertThat(ready.await(10, SECONDS))
                        .as("both database transactions should be ready")
                        .isTrue();
            } finally {
                start.countDown();
            }
            return List.of(first.get(20, SECONDS), second.get(20, SECONDS));
        }
    }

    private InsertResult insertInTransaction(
            String insert, StatementBinder binder, CountDownLatch ready, CountDownLatch start) throws Exception {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement = connection.prepareStatement(insert)) {
                binder.bind(statement);
                ready.countDown();
                if (!start.await(10, SECONDS)) {
                    throw new IllegalStateException("Timed out waiting to start concurrent inserts");
                }
                statement.executeUpdate();
                connection.commit();
                return InsertResult.success();
            } catch (SQLException exception) {
                connection.rollback();
                return InsertResult.rejected(exception);
            }
        }
    }

    private void assertOneAcceptedAndOneRejected(
            List<InsertResult> results, String expectedSqlState, String expectedConstraint) {
        assertThat(results).filteredOn(InsertResult::accepted).hasSize(1);
        assertThat(results)
                .filteredOn(result -> !result.accepted())
                .singleElement()
                .satisfies(result -> {
                    assertThat(result.sqlState()).isEqualTo(expectedSqlState);
                    assertThat(result.message()).contains(expectedConstraint);
                });
    }

    private void clearPersistedData() {
        jdbcClient.sql("DELETE FROM incident").update();
        jdbcClient.sql("DELETE FROM holiday").update();
        jdbcClient.sql("DELETE FROM on_call_period").update();
        jdbcClient.sql("DELETE FROM engineer_profile").update();
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private record InsertResult(boolean accepted, String sqlState, String message) {

        private static InsertResult success() {
            return new InsertResult(true, null, null);
        }

        private static InsertResult rejected(SQLException exception) {
            return new InsertResult(false, exception.getSQLState(), exception.getMessage());
        }
    }
}
