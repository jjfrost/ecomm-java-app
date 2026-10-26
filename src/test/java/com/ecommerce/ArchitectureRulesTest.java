package com.ecommerce;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.ecommerce",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {
    @ArchTest
    static final ArchRule jdbc_is_confined_to_oracle_gateways =
            noClasses()
                    .that()
                    .resideOutsideOfPackages(
                            "..oracle..",
                            "..gateway..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("java.sql..", "javax.sql..");

    @ArchTest
    static final ArchRule controllers_do_not_access_repositories =
            noClasses()
                    .that()
                    .resideInAnyPackage("..controller..", "..api..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule repositories_are_interfaces =
            classes()
                    .that()
                    .resideInAnyPackage("..repository..")
                    .and()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .beInterfaces();
}
