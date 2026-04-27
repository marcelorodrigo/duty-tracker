package com.dutytracker.gateway.controllers.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.dutytracker.domain.EmployeeType;
import com.dutytracker.usecase.profile.*;
import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.response.profile.*;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ProfileController.class)
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
                EmployeeType.INTERNAL,
                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false);
    }

    @Test
    @DisplayName("POST /api/v1/profile returns 201 with created profile")
    void shouldCreateProfile() {
        var response = sampleProfile();

        given(createProfileUseCase.execute(any(CreateEngineerProfileRequest.class)))
                .willReturn(response);

        var json = """
                {
                  "employeeType": "INTERNAL",
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
        var response = sampleProfile();

        given(getProfileUseCase.execute(any(GetEngineerProfileRequest.class))).willReturn(response);

        assertThat(mvc.get().uri("/api/v1/profile"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.employeeType()).isEqualTo(EmployeeType.INTERNAL);
                    assertThat(res.locked()).isFalse();
                });
    }

    @Test
    @DisplayName("PUT /api/v1/profile returns 200 with updated profile")
    void shouldUpdateProfile() {
        var updated = new EngineerProfileResponse(
                1L,
                EmployeeType.EXTERNAL,
                List.of("MONDAY", "TUESDAY", "WEDNESDAY"),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                false);

        given(updateProfileUseCase.execute(any(UpdateEngineerProfileRequest.class)))
                .willReturn(updated);

        var json = """
                {
                  "employeeType": "EXTERNAL",
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
                .satisfies(res -> assertThat(res.employeeType()).isEqualTo(EmployeeType.EXTERNAL));
    }

    @Test
    @DisplayName("DELETE /api/v1/profile returns 204 No Content")
    void shouldDeleteProfile() {
        assertThat(mvc.delete().uri("/api/v1/profile")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteProfileUseCase).execute(any(DeleteEngineerProfileRequest.class));
    }
}
