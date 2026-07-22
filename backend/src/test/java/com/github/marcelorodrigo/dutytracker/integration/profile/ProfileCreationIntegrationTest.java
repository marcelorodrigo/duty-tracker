package com.github.marcelorodrigo.dutytracker.integration.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.gateway.postgres.repository.EngineerProfileJpaRepository;
import com.github.marcelorodrigo.dutytracker.testsupport.PostgreSqlContainerTestSupport;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileCreationIntegrationTest extends PostgreSqlContainerTestSupport {

    private static final String PROFILE_REQUEST = """
            {
              "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"],
              "workStartTime": "08:30:00",
              "workEndTime": "17:00:00",
              "hourlyRate": 50.00,
              "standbyWeekdaySaturdayPercentage": 0.067,
              "standbyWeekdaySundayHolidayPercentage": 0.084
            }
            """;

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private EngineerProfileJpaRepository profileRepository;

    @Test
    @DisplayName("should create a profile through the API on a fresh migrated database")
    void shouldCreateProfileThroughApiOnFreshMigratedDatabase() {
        // given
        var profileCountBeforeCreation = profileRepository.count();

        // when
        var creation = mvc.post()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_REQUEST);

        // then
        assertThat(profileCountBeforeCreation).isZero();
        assertThat(creation)
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/profile")
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(profile -> {
                    assertThat(profile.id()).isPositive();
                    assertThat(profile.workingDays())
                            .containsExactlyElementsOf(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"));
                    assertThat(profile.workStartTime()).isEqualTo(LocalTime.of(8, 30));
                    assertThat(profile.workEndTime()).isEqualTo(LocalTime.of(17, 0));
                    assertThat(profile.hourlyRate()).isEqualByComparingTo(new BigDecimal("50.00"));
                });
        assertThat(profileRepository.count()).isOne();
    }

    @Test
    @DisplayName("should reject a second profile created through the API")
    void shouldRejectSecondProfileCreatedThroughApi() {
        // given
        mvc.post()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_REQUEST)
                .exchange();

        // when
        var secondCreation = mvc.post()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_REQUEST);

        // then
        assertThat(secondCreation).hasStatus(HttpStatus.CONFLICT);
        assertThat(profileRepository.count()).isOne();
    }
}
