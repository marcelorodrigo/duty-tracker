package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.preferences.*;
import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(PreferencesController.class)
class PreferencesControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetUserPreferencesUseCase getUserPreferencesUseCase;

    @MockitoBean
    private UpdateUserPreferencesUseCase updateUserPreferencesUseCase;

    private UserPreferencesResponse samplePreferences() {
        return new UserPreferencesResponse(ColorScheme.AUTO, OnboardingStep.COMPLETE);
    }

    @Test
    @DisplayName("GET /api/v1/preferences returns 200 with preferences")
    void shouldReturnPreferences() {
        given(getUserPreferencesUseCase.execute(any(GetUserPreferencesRequest.class)))
                .willReturn(samplePreferences());

        assertThat(mvc.get().uri("/api/v1/preferences"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(UserPreferencesResponse.class)
                .satisfies(res -> assertThat(res.colorScheme()).isEqualTo(ColorScheme.AUTO));
    }

    @Test
    @DisplayName("PUT /api/v1/preferences returns 200 with updated preferences")
    void shouldUpdatePreferences() {
        var updated = new UserPreferencesResponse(ColorScheme.DARK, OnboardingStep.COMPLETE);
        given(updateUserPreferencesUseCase.execute(any(UpdateUserPreferencesRequest.class)))
                .willReturn(updated);

        var json = """
                {
                  "colorScheme": "DARK"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(UserPreferencesResponse.class)
                .satisfies(res -> assertThat(res.colorScheme()).isEqualTo(ColorScheme.DARK));
    }
}
