package ma.ensam.clientpulse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie le socle de persistance sur un vrai MySQL :
 *
 *  1. Flyway applique V1__init.sql puis V2__roles.sql ;
 *  2. Hibernate (ddl-auto=validate) accepte le schéma créé par Flyway ;
 *  3. les rôles viennent bien de la migration et non du DataInitializer ;
 *  4. les données d'amorçage (admin + commercial) sont créées au-dessus.
 *
 * Le test est ignoré si Docker n'est pas disponible, pour ne pas bloquer
 * un développeur qui n'a que MySQL en local.
 */
@EnabledIfDockerAvailable
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "app.seed.enabled=true"
})
@Import(TestcontainersConfiguration.class)
class FlywaySchemaTest {

    private static final List<String> TABLES_ATTENDUES = List.of(
            "roles",
            "utilisateurs",
            "utilisateurs_roles",
            "commerciaux",
            "clients",
            "interactions",
            "sequence_numerotation",
            "devis",
            "ligne_devis",
            "facture",
            "ligne_facture",
            "paiement",
            "parametres_scoring",
            "scores_client",
            "relances"
    );

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flyway_a_applique_toutes_les_migrations() {
        List<String> versions = jdbc.queryForList(
                "SELECT version FROM flyway_schema_history "
                        + "WHERE success = 1 ORDER BY installed_rank",
                String.class
        );

        assertThat(versions).containsExactly("1", "2", "4", "5");
    }

    @Test
    void toutes_les_tables_du_modele_existent() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE()",
                String.class
        );

        assertThat(tables).containsAll(TABLES_ATTENDUES);
    }

    @Test
    void les_roles_sont_inseres_par_la_migration_v2() {
        List<String> roles = jdbc.queryForList(
                "SELECT nom FROM roles ORDER BY nom",
                String.class
        );

        assertThat(roles).containsExactly(
                "ADMIN", "ANALYSTE", "COMMERCIAL", "COMPTABLE", "MANAGER"
        );
    }

    @Test
    void le_compte_admin_est_cree_avec_son_role() {
        String roleAdmin = jdbc.queryForObject(
                "SELECT r.nom FROM utilisateurs u "
                        + "JOIN utilisateurs_roles ur ON ur.utilisateur_id = u.id "
                        + "JOIN roles r ON r.id = ur.role_id "
                        + "WHERE u.email = 'admin@clientpulse.ma'",
                String.class
        );

        assertThat(roleAdmin).isEqualTo("ADMIN");
    }
}
