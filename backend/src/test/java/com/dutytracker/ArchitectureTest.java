package com.dutytracker.ArchitectureTest.java;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.dutytracker", importOptions = {
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars.class,
        com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeArchives.class
})
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainAndUsecaseShouldNotImportGatewayOrInfrastructure = noClasses()
            .that().resideInAnyPackage(
                    "com.dutytracker.domain..",
                    "com.dutytracker.usecase..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.dutytracker.gateway..",
                    "com.dutytracker.infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule noAutowiredFieldInjection = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .allowEmptyShould(true);
}
