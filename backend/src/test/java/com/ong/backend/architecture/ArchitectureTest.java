package com.ong.backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ong.backend");
    }

    @Test
    @DisplayName("Domain layer must not depend on Spring Framework")
    void domainMustNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("A camada de domínio deve ser pura e desacoplada de frameworks web/IoC.")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on Jakarta Persistence (JPA)")
    void domainMustNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .because("A camada de domínio não deve ter acoplamento com o mecanismo de persistência.")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer must not depend on Web or Infrastructure layers")
    void domainMustNotDependOnOuterLayers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..web..", "..infrastructure..")
                .because("As dependências devem apontar sempre para dentro (Clean Architecture / Inversão de Dependência).")
                .check(importedClasses);
    }
}
