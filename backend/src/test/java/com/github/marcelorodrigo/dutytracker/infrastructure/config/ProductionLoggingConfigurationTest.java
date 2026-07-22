package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.logback.StructuredLogEncoder;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.Environment;
import tools.jackson.databind.ObjectMapper;

class ProductionLoggingConfigurationTest {

    private static final String STRUCTURED_CONSOLE_FORMAT = "logging.structured.format.console";
    private static final String LOG_MESSAGE = "Incident overtime entries calculation requested";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ApplicationContextRunner productionContext = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=production");

    private final ApplicationContextRunner developmentContext = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=development");

    private record LogContext(long incidentId, String correlationId, String databasePassword) {}

    @Test
    @DisplayName("should emit production log context as sanitized JSON fields")
    void shouldEmitProductionLogContextAsSanitizedJsonFields() {
        // given
        var context = new LogContext(42L, "incident-42", "super-secret");

        productionContext.run(applicationContext -> {
            // given
            var environment = applicationContext.getEnvironment();

            // when
            var output = emitStructuredEvent(environment, context);
            var event = JSON.readTree(output);

            // then
            assertThat(environment.getProperty(STRUCTURED_CONSOLE_FORMAT)).isEqualTo("ecs");
            assertThat(event.has("incidentId")).isTrue();
            assertThat(event.get("log").get("level").stringValue()).isEqualTo("INFO");
            assertThat(event.get("service").get("name").stringValue()).isEqualTo("duty-tracker-backend");
            assertThat(event.get("service").get("environment").stringValue()).isEqualTo("production");
            assertThat(event.get("incidentId").longValue()).isEqualTo(context.incidentId());
            assertThat(event.get("correlationId").stringValue()).isEqualTo(context.correlationId());
            assertThat(event.get("message").stringValue())
                    .isEqualTo(LOG_MESSAGE)
                    .doesNotContain(Long.toString(context.incidentId()), context.correlationId());
            assertThat(event.has("databasePassword")).isFalse();
            assertThat(output).doesNotContain(context.databasePassword());
        });
    }

    @Test
    @DisplayName("should keep structured console formatting disabled for development profile")
    void shouldKeepStructuredConsoleFormattingDisabledForDevelopmentProfile() {
        developmentContext.run(applicationContext -> {
            // given
            var environment = applicationContext.getEnvironment();

            // when
            var format = environment.getProperty(STRUCTURED_CONSOLE_FORMAT);

            // then
            assertThat(format).isNull();
        });
    }

    private static String emitStructuredEvent(Environment environment, LogContext context) {
        var loggerContext = new LoggerContext();
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());
        loggerContext.putObject(Environment.class.getName(), environment);
        loggerContext.start();

        var output = new ByteArrayOutputStream();
        var encoder = new StructuredLogEncoder();
        encoder.setContext(loggerContext);
        encoder.setFormat(environment.getRequiredProperty(STRUCTURED_CONSOLE_FORMAT));
        encoder.start();

        var appender = new OutputStreamAppender<ILoggingEvent>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.setOutputStream(output);
        appender.start();

        var logger = loggerContext.getLogger(ProductionLoggingConfigurationTest.class);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);
        logger.addAppender(appender);

        loggerContext.getMDCAdapter().put("correlationId", context.correlationId());
        try {
            logger.atInfo().addKeyValue("incidentId", context.incidentId()).log(LOG_MESSAGE);
        } finally {
            loggerContext.getMDCAdapter().remove("correlationId");
        }

        logger.detachAppender(appender);
        appender.stop();
        encoder.stop();
        loggerContext.stop();

        return output.toString(StandardCharsets.UTF_8).strip();
    }
}
