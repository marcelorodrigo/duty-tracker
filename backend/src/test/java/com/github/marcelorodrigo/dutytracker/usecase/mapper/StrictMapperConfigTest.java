package com.github.marcelorodrigo.dutytracker.usecase.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.ERROR;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;

class StrictMapperConfigTest {

    private static final String PRODUCTION_PACKAGE = "com.github.marcelorodrigo.dutytracker";

    @Test
    @DisplayName("should configure strict Spring mapper generation")
    void shouldConfigureStrictSpringMapperGeneration() {
        // given
        var productionClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(PRODUCTION_PACKAGE);
        var configClass = productionClasses.get(StrictMapperConfig.class);

        // when
        var mapperConfig = configClass.getAnnotationOfType(MapperConfig.class);

        // then
        assertThat(mapperConfig.componentModel()).isEqualTo(SPRING);
        assertThat(mapperConfig.unmappedTargetPolicy()).isEqualTo(ERROR);
    }

    @Test
    @DisplayName("should require every production mapper to reference the strict config")
    void shouldRequireEveryProductionMapperToReferenceTheStrictConfig() {
        // given
        var productionClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(PRODUCTION_PACKAGE);

        // when
        var mapperConfigs = productionClasses.stream()
                .filter(javaClass -> javaClass.isAnnotatedWith(Mapper.class))
                .collect(Collectors.toMap(
                        JavaClass::getName,
                        javaClass -> javaClass.getAnnotationOfType(Mapper.class).config()));

        // then
        assertThat(mapperConfigs)
                .isNotEmpty()
                .allSatisfy((mapperName, config) ->
                        assertThat(config).as("%s shared config", mapperName).isEqualTo(StrictMapperConfig.class));
    }
}
