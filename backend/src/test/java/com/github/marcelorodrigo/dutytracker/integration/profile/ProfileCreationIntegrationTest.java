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

@SpringBootTest(
        properties = {
            "app.profile-defaults.hourly-rate=42.50",
            "app.profile-defaults.standby-weekday-saturday-percentage=0.071",
            "app.profile-defaults.standby-weekday-sunday-holiday-percentage=0.095"
        })
@AutoConfigureMockMvc
@Transactional
class ProfileCreationIntegrationTest extends PostgreSqlContainerTestSupport {

    private static final String PROFILE_REQUEST_WITHOUT_DEFAULTS = """
            {
              "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"],
              "workStartTime": "08:30:00",
              "workEndTime": "17:00:00"
            }
            """;

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private EngineerProfileJpaRepository profileRepository;

    @Test
    @DisplayName("should create a profile with configured defaults on a fresh migrated database")
    void shouldCreateProfileWithConfiguredDefaultsOnFreshMigratedDatabase() {
        // given
        var profileCountBeforeCreation = profileRepository.count();

        // when
        var creation = mvc.post()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_REQUEST_WITHOUT_DEFAULTS);

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
                    assertThat(profile.hourlyRate()).isEqualByComparingTo(new BigDecimal("42.50"));
                    assertThat(profile.standbyWeekdaySaturdayPercentage())
                            .isEqualByComparingTo(new BigDecimal("0.071"));
                    assertThat(profile.standbyWeekdaySundayHolidayPercentage())
                            .isEqualByComparingTo(new BigDecimal("0.095"));
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
                .content(PROFILE_REQUEST_WITHOUT_DEFAULTS)
                .exchange();

        // when
        var secondCreation = mvc.post()
                .uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROFILE_REQUEST_WITHOUT_DEFAULTS);

        // then
        assertThat(secondCreation).hasStatus(HttpStatus.CONFLICT);
        assertThat(profileRepository.count()).isOne();
    }
}
