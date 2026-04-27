package com.dutytracker.infrastructure.persistence.entity;

import com.dutytracker.domain.model.ColorScheme;
import com.dutytracker.domain.model.OnboardingStep;
import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferencesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ColorScheme colorScheme;

    @Enumerated(EnumType.STRING)
    private OnboardingStep onboardingStep;

    public UserPreferencesEntity() {
    }

    public UserPreferencesEntity(Long id, ColorScheme colorScheme, OnboardingStep onboardingStep) {
        this.id = id;
        this.colorScheme = colorScheme;
        this.onboardingStep = onboardingStep;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public OnboardingStep getOnboardingStep() {
        return onboardingStep;
    }

    public void setOnboardingStep(OnboardingStep onboardingStep) {
        this.onboardingStep = onboardingStep;
    }
}
