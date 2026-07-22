package com.github.marcelorodrigo.dutytracker.gateway.postgres;

import com.github.marcelorodrigo.dutytracker.testsupport.PostgreSqlContainerTestSupport;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class PostgreSqlRepositoryTestSupport extends PostgreSqlContainerTestSupport {}
