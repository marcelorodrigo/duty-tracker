package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ApiDocumentationPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(ApiDocumentationTestConfiguration.class);

    @Test
    @DisplayName("should disable API documentation by default")
    void shouldDisableApiDocumentationByDefault() {
        // given
        var runner = contextRunner;

        // when
        runner.run(context -> {
            var properties = context.getBean(ApiDocumentationProperties.class);

            // then
            assertThat(properties.enabled()).isFalse();
            assertThat(context.getEnvironment().getProperty("springdoc.api-docs.enabled", Boolean.class))
                    .isFalse();
            assertThat(context.getEnvironment().getProperty("springdoc.swagger-ui.enabled", Boolean.class))
                    .isFalse();
        });
    }

    @Test
    @DisplayName("should enable API documentation for the development profile")
    void shouldEnableApiDocumentationForDevelopmentProfile() {
        // given
        var runner = contextRunner.withPropertyValues("spring.profiles.active=development");

        // when
        runner.run(context -> {
            var properties = context.getBean(ApiDocumentationProperties.class);

            // then
            assertThat(properties.enabled()).isTrue();
            assertThat(context.getEnvironment().getProperty("springdoc.api-docs.enabled", Boolean.class))
                    .isTrue();
            assertThat(context.getEnvironment().getProperty("springdoc.swagger-ui.enabled", Boolean.class))
                    .isTrue();
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ApiDocumentationProperties.class)
    static class ApiDocumentationTestConfiguration {}
}
