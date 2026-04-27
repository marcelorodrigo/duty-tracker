package com.dutytracker.gateway.postgres.entity;

import com.dutytracker.domain.ColorScheme;
import com.dutytracker.domain.OnboardingStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ColorScheme colorScheme;

    @Enumerated(EnumType.STRING)
    private OnboardingStep onboardingStep;
}
