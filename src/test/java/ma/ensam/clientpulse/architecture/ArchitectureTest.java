package ma.ensam.clientpulse.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Vérifie que les règles de dépendance entre couches décrites dans le README
 * (section 3) sont réellement respectées. Le build échoue si une règle est violée.
 *
 * Couche présentation -> service (interfaces) + dto
 * Couche service      -> dao (interfaces) + domain + dto
 * Couche dao          -> domain
 * Couche domain       -> aucune autre couche
 */
@AnalyzeClasses(
        packages = "ma.ensam.clientpulse",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule la_presentation_n_accede_pas_aux_couches_basses =
            noClasses()
                    .that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..dao..", "..service.impl..", "..domain.entity..")
                    .because("un contrôleur ne parle qu'aux interfaces de service et aux DTO");

    @ArchTest
    static final ArchRule le_service_ne_depend_pas_de_la_presentation =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..presentation..")
                    .because("la logique métier ne connaît pas HTTP");

    @ArchTest
    static final ArchRule le_dao_ne_depend_pas_du_service_ni_de_la_presentation =
            noClasses()
                    .that().resideInAPackage("..dao..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..service..", "..presentation..")
                    .because("l'accès aux données ne contient aucune règle métier");

    @ArchTest
    static final ArchRule le_domaine_ne_depend_d_aucune_autre_couche =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..service..", "..dao..", "..presentation..")
                    .because("les entités décrivent le métier, elles ne connaissent ni la base ni l'API");

    @ArchTest
    static final ArchRule les_mappers_ne_lisent_pas_la_base =
            noClasses()
                    .that().resideInAPackage("..service.mapper..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..dao..")
                    .because("un mapper ne fait que convertir une entité en DTO, sans requête");
}
