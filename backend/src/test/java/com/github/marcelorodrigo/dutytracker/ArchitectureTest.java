package com.github.marcelorodrigo.dutytracker;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateResponseMapper;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHoliday;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = ArchitectureTest.BASE_PACKAGE,
        importOptions = {
            ImportOption.DoNotIncludeTests.class,
            ImportOption.DoNotIncludeJars.class,
            ImportOption.DoNotIncludeArchives.class
        })
class ArchitectureTest {

    static final String BASE_PACKAGE = "com.github.marcelorodrigo.dutytracker";

    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String USECASE_PACKAGE = BASE_PACKAGE + ".usecase..";
    private static final String GATEWAY_PACKAGE = BASE_PACKAGE + ".gateway..";
    private static final String INFRASTRUCTURE_PACKAGE = BASE_PACKAGE + ".infrastructure..";
    private static final String CONFIGURATION_PACKAGE = BASE_PACKAGE + ".configuration..";

    private static final DescribedPredicate<JavaClass> GATEWAY_PORT_CONTRACTS = equivalentTo(
                    CompensationRateGateway.class)
            .or(equivalentTo(CompensationRateResponseMapper.class))
            .or(equivalentTo(PublicHolidayGateway.class))
            .or(equivalentTo(PublicHoliday.class))
            .or(equivalentTo(IncidentGateway.class))
            .or(equivalentTo(HolidayGateway.class))
            .or(equivalentTo(OnCallPeriodGateway.class))
            .or(equivalentTo(EngineerProfileGateway.class))
            .as("application-facing gateway port contracts");

    private static final DescribedPredicate<JavaClass> OUTER_LAYER_CLASSES = resideInAnyPackage(
                    USECASE_PACKAGE, GATEWAY_PACKAGE, INFRASTRUCTURE_PACKAGE, CONFIGURATION_PACKAGE)
            .as("use-case, gateway, or infrastructure classes");

    private static final DescribedPredicate<JavaClass> NON_PORT_GATEWAY_CLASSES = resideInAPackage(GATEWAY_PACKAGE)
            .and(not(GATEWAY_PORT_CONTRACTS))
            .as("gateway classes outside the application-facing port contracts");

    private static final DescribedPredicate<JavaClass> ADAPTER_AND_INFRASTRUCTURE_CLASSES = NON_PORT_GATEWAY_CLASSES
            .or(resideInAnyPackage(INFRASTRUCTURE_PACKAGE, CONFIGURATION_PACKAGE))
            .as("adapter or infrastructure classes");

    @ArchTest
    static final ArchRule domainShouldNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat(OUTER_LAYER_CLASSES)
            .because("the domain is the innermost layer");

    @ArchTest
    static final ArchRule useCasesShouldOnlyDependOnGatewayPorts = noClasses()
            .that()
            .resideInAPackage(USECASE_PACKAGE)
            .should()
            .dependOnClassesThat(ADAPTER_AND_INFRASTRUCTURE_CLASSES)
            .because("use cases may depend on gateway ports, but never on adapter implementations");

    @ArchTest
    static final ArchRule noAutowiredFieldInjection =
            noFields().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule requestValidatorsShouldNotAccessGateways = noClasses()
            .that()
            .resideInAPackage(BASE_PACKAGE + ".usecase.validator..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(GATEWAY_PACKAGE);
}
