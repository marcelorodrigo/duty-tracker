package com.github.marcelorodrigo.dutytracker.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class PostgreSqlContainerTestSupport {

    private static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:18-alpine");

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    protected static void configurePostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }
}
