package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@ExtendWith(MockitoExtension.class)
class CorsConfigurationTest {

    private CorsRegistry registry;
    private CorsRegistration registration;

    @BeforeEach
    void setUp() {
        registry = mock(CorsRegistry.class);
        registration = mock(CorsRegistration.class, Answers.RETURNS_SELF);
        when(registry.addMapping("/api/**")).thenReturn(registration);
    }

    @Test
    @DisplayName("should register /api/** mapping with the configured origins")
    void shouldRegisterConfiguredOrigins() {
        // given
        var origins = List.of("https://app.example.com", "https://staging.example.com");
        var appProperties = new AppProperties("https://api.example.com", new AppProperties.CorsProperties(origins));
        var underTest = new CorsConfiguration(appProperties);

        // when
        underTest.addCorsMappings(registry);

        // then
        verify(registry).addMapping("/api/**");
        verify(registration).allowedOrigins("https://app.example.com", "https://staging.example.com");
    }

    @ParameterizedTest
    @DisplayName("should apply the configured origin list to the CORS mapping")
    @CsvSource({
        "http://localhost:3000, http://localhost:3000",
        "'https://a.com,https://b.com', 'https://a.com,https://b.com'"
    })
    void shouldApplyConfiguredOriginList(String csvOrigins, String expectedCsv) {
        // given
        var origins = List.of(csvOrigins.split(","));
        var expected = List.of(expectedCsv.split(","));
        var appProperties = new AppProperties("https://api.example.com", new AppProperties.CorsProperties(origins));
        var underTest = new CorsConfiguration(appProperties);

        // when
        underTest.addCorsMappings(registry);

        // then
        verify(registration).allowedOrigins(expected.toArray(new String[0]));
    }

    @Test
    @DisplayName("should include localhost:3000 by default when no origins configured")
    void shouldDefaultToLocalhostOrigin() {
        // given
        var appProperties = new AppProperties("https://api.example.com", new AppProperties.CorsProperties(null));
        var underTest = new CorsConfiguration(appProperties);

        // when
        underTest.addCorsMappings(registry);

        // then
        verify(registration).allowedOrigins("http://localhost:3000");
    }
}
