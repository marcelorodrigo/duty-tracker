package com.github.marcelorodrigo.dutytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.usecase.architecturefixture.ForbiddenAdapterDependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArchitectureRuleRegressionTest {

    @Test
    @DisplayName("should import production classes from the configured base package")
    void shouldImportProductionClassesFromConfiguredBasePackage() {
        // given
        var importer = productionClassImporter();

        // when
        var classes = importer.importPackages(ArchitectureTest.BASE_PACKAGE);

        // then
        assertThat(classes)
                .isNotEmpty()
                .extracting(JavaClass::getName)
                .contains(DutyTrackerApplication.class.getName())
                .noneMatch(name -> name.contains("architecturefixture"));
    }

    @Test
    @DisplayName("should reject an intentional dependency from a use case to an adapter")
    void shouldRejectIntentionalDependencyFromUseCaseToAdapter() {
        // given
        var classes = new ClassFileImporter().importClasses(ForbiddenAdapterDependency.class);

        // when / then
        assertThatThrownBy(() -> ArchitectureTest.useCasesShouldOnlyDependOnGatewayPorts.check(classes))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(ForbiddenAdapterDependency.class.getName())
                .hasMessageContaining("IncidentEntity");
    }

    @Test
    @DisplayName("should reject an architecture rule that matches no classes")
    void shouldRejectArchitectureRuleThatMatchesNoClasses() {
        // given
        var classes = new ClassFileImporter().importClasses(ArchitectureRuleRegressionTest.class);

        // when / then
        assertThatThrownBy(() -> ArchitectureTest.useCasesShouldOnlyDependOnGatewayPorts.check(classes))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("failed to check any classes");
    }

    private ClassFileImporter productionClassImporter() {
        return new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .withImportOption(new ImportOption.DoNotIncludeJars())
                .withImportOption(new ImportOption.DoNotIncludeArchives());
    }
}
