package org.nordicthings.stocktracker.inventory

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Test

class ArchitectureRulesTest {

    private val inventoryClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("org.nordicthings.stocktracker.inventory")

    @Test
    fun `domain does not depend on technical frameworks or outer rings`() {
        noClasses()
            .that().resideInAPackage("..inventory.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "org.hibernate..",
                "org.thymeleaf..",
                "org.nordicthings.stocktracker.inventory.application..",
                "org.nordicthings.stocktracker.inventory.adapter..",
            )
            .check(inventoryClasses)
    }

    @Test
    fun `application does not depend on adapters`() {
        noClasses()
            .that().resideInAPackage("..inventory.application..")
            .should().dependOnClassesThat().resideInAPackage("..inventory.adapter..")
            .check(inventoryClasses)
    }

    @Test
    fun `web adapter does not depend on persistence adapter`() {
        noClasses()
            .that().resideInAPackage("..inventory.adapter.web..")
            .should().dependOnClassesThat().resideInAPackage("..inventory.adapter.persistence..")
            .check(inventoryClasses)
    }

    @Test
    fun `persistence adapter does not depend on web adapter`() {
        noClasses()
            .that().resideInAPackage("..inventory.adapter.persistence..")
            .should().dependOnClassesThat().resideInAPackage("..inventory.adapter.web..")
            .check(inventoryClasses)
    }
}
