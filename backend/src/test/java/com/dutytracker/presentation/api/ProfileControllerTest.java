package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.profile.*;
import com.dutytracker.domain.exception.ProfileLockedException;
import com.dutytracker.domain.model.EmployeeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateEngineerProfileUseCase createEngineerProfileUseCase;

    @MockitoBean
    private GetEngineerProfileUseCase getEngineerProfileUseCase;

    @MockitoBean
    private UpdateEngineerProfileUseCase updateEngineerProfileUseCase;

    private EngineerProfileResponse sampleProfile() {
        return new EngineerProfileResponse(1L, EmployeeType.INTERNAL,
                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                java.time.LocalTime.of(9, 0), java.time.LocalTime.of(17, 0), false);
    }

    @Test
    @DisplayName("POST /api/v1/profile returns 201 Created with profile body")
    void shouldCreateProfile() {
        given(createEngineerProfileUseCase.execute(any(CreateEngineerProfileRequest.class)))
                .willReturn(sampleProfile());

        var json = """
                {
                  "employeeType": "INTERNAL",
                  "workingDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"],
                  "workStartTime": "09:00",
                  "workEndTime": "17:00"
                }
                """;

        assertThat(mvc.post().uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/profile");
    }

    @Test
    @DisplayName("GET /api/v1/profile returns 200 with profile")
    void shouldReturnProfile() {
        given(getEngineerProfileUseCase.execute(any(GetEngineerProfileRequest.class)))
                .willReturn(sampleProfile());

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
        given(updateEngineerProfileUseCase.execute(any(UpdateEngineerProfileRequest.class)))
                .willReturn(sampleProfile());

        var json = """
                {
                  "employeeType": "INTERNAL",
                  "workingDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"],
                  "workStartTime": "09:00",
                  "workEndTime": "17:00"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(EngineerProfileResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/profile returns 409 when profile is locked")
    void shouldReturn409WhenProfileLocked() {
        given(updateEngineerProfileUseCase.execute(any(UpdateEngineerProfileRequest.class)))
                .willThrow(new ProfileLockedException("Profile is locked"));

        var json = """
                {
                  "employeeType": "INTERNAL",
                  "workingDays": ["MONDAY"],
                  "workStartTime": "09:00",
                  "workEndTime": "17:00"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CONFLICT);
    }
}
