package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.onboarding.*;
import com.dutytracker.domain.model.OnboardingStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetOnboardingStatusUseCase getOnboardingStatusUseCase;

    @MockitoBean
    private AdvanceOnboardingStepUseCase advanceOnboardingStepUseCase;

    @Test
    @DisplayName("GET /api/v1/onboarding returns 200 with step and completed flag")
    void shouldReturnOnboardingStatus() {
        var response = new OnboardingStatusResponse(OnboardingStep.PROFILE, false);
        given(getOnboardingStatusUseCase.execute(any(GetOnboardingStatusRequest.class))).willReturn(response);

        assertThat(mvc.get().uri("/api/v1/onboarding"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnboardingStatusResponse.class)
                .satisfies(res -> {
                    assertThat(res.step()).isEqualTo(OnboardingStep.PROFILE);
                    assertThat(res.completed()).isFalse();
                });
    }

    @Test
    @DisplayName("POST /api/v1/onboarding returns 200 with updated step")
    void shouldAdvanceOnboardingStep() {
        var response = new OnboardingStatusResponse(OnboardingStep.PREFERENCES, false);
        given(advanceOnboardingStepUseCase.execute(any(AdvanceOnboardingStepRequest.class))).willReturn(response);

        var json = """
                {
                  "currentStep": "PROFILE"
                }
                """;

        assertThat(mvc.post().uri("/api/v1/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnboardingStatusResponse.class)
                .satisfies(res -> {
                    assertThat(res.step()).isEqualTo(OnboardingStep.PREFERENCES);
                    assertThat(res.completed()).isFalse();
                });
    }
}
