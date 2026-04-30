package com.dutytracker.gateway.controllers.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.dutytracker.usecase.profile.*;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ProfileController.class)
@Import(GlobalExceptionHandler.class)
class ProfileControllerTest {

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
                LocalTime.of(17, 0));
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
                1L, List.of("MONDAY", "TUESDAY", "WEDNESDAY"), LocalTime.of(8, 0), LocalTime.of(16, 0));

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
    @DisplayName("GET /api/v1/profile returns 404 when no profile exists")
    void shouldReturnNotFoundWhenNoProfile() {
        given(getProfileUseCase.execute(any(GetEngineerProfileRequest.class))).willReturn(null);

        assertThat(mvc.get().uri("/api/v1/profile")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /api/v1/profile returns 204 No Content")
    void shouldDeleteProfile() {
        assertThat(mvc.delete().uri("/api/v1/profile")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteProfileUseCase).execute(any(DeleteEngineerProfileRequest.class));
    }
}
