package com.github.marcelorodrigo.dutytracker;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.github.marcelorodrigo.dutytracker",
        importOptions = {
            ImportOption.DoNotIncludeTests.class,
            ImportOption.DoNotIncludeJars.class,
            ImportOption.DoNotIncludeArchives.class
        })
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainAndUsecaseShouldNotImportInfrastructure = noClasses()
            .that()
            .resideInAnyPackage(
                    "com.github.marcelorodrigo.dutytracker.domain..", "com.github.marcelorodrigo.dutytracker.usecase..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.github.marcelorodrigo.dutytracker.infrastructure..",
                    "com.github.marcelorodrigo.dutytracker.gateway.postgres..",
                    "com.github.marcelorodrigo.dutytracker.gateway.controllers..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule noAutowiredFieldInjection = noFields()
            .should()
            .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule requestValidatorsShouldNotAccessGateways = noClasses()
            .that()
            .resideInAPackage("com.github.marcelorodrigo.dutytracker.usecase.validator..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.github.marcelorodrigo.dutytracker.gateway..");
}
