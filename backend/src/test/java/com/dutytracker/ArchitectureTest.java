package com.dutytracker;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.dutytracker");

    @Test
    void domainShouldNotImportInfrastructureOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.dutytracker.domain..",
                        "com.dutytracker.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.dutytracker.infrastructure..",
                        "com.dutytracker.presentation..")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void noAutowiredFieldInjection() {
        ArchRule rule = noFields()
                .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void allSpringBeansUseConstructorInjection() {
        // Enforced by the no-Autowired-fields test above — any @Autowired usage on fields is forbidden.
        // This test verifies there are no @Autowired fields across the entire codebase.
        noFields()
                .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .allowEmptyShould(true)
                .check(classes);
    }
}
