package com.github.marcelorodrigo.dutytracker.gateway.controllers.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.profile.CreateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.DeleteEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.GetEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.profile.UpdateEngineerProfileUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.GetEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ProfileController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
class ProfileControllerTest {

    private record ProblemDetailResponse(URI type, String title, int status, String detail) {}

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateEngineerProfileUseCase createProfileUseCase;

    @MockitoBean
    private GetEngineerProfileUseCase getProfileUseCase;

    @MockitoBean
    private UpdateEngineerProfileUseCase updateProfileUseCase;

    @MockitoBean
    private DeleteEngineerProfileUseCase deleteProfileUseCase;

    private EngineerProfileResponse sampleProfile() {
        return new EngineerProfileResponse(
                1L,
                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                BigDecimal.valueOf(50.00),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"));
    }

    @Test
    @DisplayName("POST /api/v1/profile returns 201 with created profile")
    void shouldCreateProfile() {
        given(createProfileUseCase.execute(any(CreateEngineerProfileRequest.class)))
                .willReturn(sampleProfile());

        var json = """
                {
                  "workingDays": ["MONDAY"],
                  "workStartTime": "09:00:00",
                  "workEndTime": "17:00:00"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("GET /api/v1/profile returns 200 with profile")
    void shouldGetProfile() {
        given(getProfileUseCase.execute(any(GetEngineerProfileRequest.class))).willReturn(sampleProfile());

        assertThat(mvc.get().uri("/api/v1/profile"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/profile returns 200 with updated profile")
    void shouldUpdateProfile() {
        var updated = new EngineerProfileResponse(
                1L,
                List.of("MONDAY", "TUESDAY", "WEDNESDAY"),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                BigDecimal.valueOf(75.50),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"));

        given(updateProfileUseCase.execute(any(UpdateEngineerProfileRequest.class)))
                .willReturn(updated);

        var json = """
                {
                  "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY"],
                  "workStartTime": "08:00:00",
                  "workEndTime": "16:00:00"
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(res -> assertThat(res.workStartTime()).isEqualTo(LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("should return 404 Problem Detail when updating a missing profile")
    void shouldReturnNotFoundWhenUpdatingMissingProfile() {
        // given
        given(updateProfileUseCase.execute(any(UpdateEngineerProfileRequest.class)))
                .willThrow(new ProfileNotFoundException());

        var json = """
                {
                  "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY"],
                  "workStartTime": "08:00:00",
                  "workEndTime": "16:00:00"
                }
                """;

        // when / then
        assertThat(mvc.put()
                        .uri("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> {
                    assertThat(problem.type()).isEqualTo(URI.create("http://localhost:8080/errors/profile-not-found"));
                    assertThat(problem.title()).isEqualTo("Profile not found");
                    assertThat(problem.status()).isEqualTo(404);
                    assertThat(problem.detail()).isEqualTo("No engineer profile found to delete");
                });
    }

    @Test
    @DisplayName("should return standard 404 Problem Detail when getting a missing profile")
    void shouldReturnStandardNotFoundProblemWhenGettingMissingProfile() {
        // given
        given(getProfileUseCase.execute(any(GetEngineerProfileRequest.class)))
                .willThrow(new ProfileNotFoundException("Engineer profile not found"));

        // when / then
        assertThat(mvc.get().uri("/api/v1/profile"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> {
                    assertThat(problem.type()).isEqualTo(URI.create("http://localhost:8080/errors/profile-not-found"));
                    assertThat(problem.title()).isEqualTo("Profile not found");
                    assertThat(problem.status()).isEqualTo(404);
                    assertThat(problem.detail()).isEqualTo("Engineer profile not found");
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/profile returns 204 No Content")
    void shouldDeleteProfile() {
        assertThat(mvc.delete().uri("/api/v1/profile")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteProfileUseCase).execute(any(DeleteEngineerProfileRequest.class));
    }
}
