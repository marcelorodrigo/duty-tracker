package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import com.github.marcelorodrigo.dutytracker.infrastructure.config.JpaAuditingConfiguration;
import com.github.marcelorodrigo.dutytracker.testsupport.PostgreSqlContainerTestSupport;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
public abstract class PostgreSqlRepositoryTestSupport extends PostgreSqlContainerTestSupport {}
