package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.entity.LigneCalculable;
import ma.ensam.clientpulse.dto.Totaux;
import ma.ensam.clientpulse.service.impl.CalculateurMontantsImpl;
import ma.ensam.clientpulse.service.interfaces.CalculateurMontants;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Test unitaire simple : pas de Spring, pas de base de données. On crée l'objet avec "new". */
class CalculateurMontantsTest {

    private final CalculateurMontants calculateur = new CalculateurMontantsImpl();

    /** Petite ligne de test qui respecte le contrat LigneCalculable. */
    private static class LigneTest implements LigneCalculable {
        private final BigDecimal quantite;
        private final BigDecimal prix;
        private final BigDecimal remise;
        private final BigDecimal taux;

        LigneTest(String quantite, String prix, String remise, String taux) {
            this.quantite = new BigDecimal(quantite);
            this.prix = new BigDecimal(prix);
            this.remise = remise == null ? null : new BigDecimal(remise);
            this.taux = new BigDecimal(taux);
        }

        @Override public BigDecimal getQuantite() { return quantite; }
        @Override public BigDecimal getPrixUnitaireHT() { return prix; }
        @Override public BigDecimal getRemise() { return remise; }
        @Override public BigDecimal getTauxTVA() { return taux; }
    }

    @Test
    void uneLigneSansRemise() {
        // 2 x 100 = 200 HT ; TVA 20 % = 40 ; TTC = 240
        Totaux t = calculateur.calculer(List.of(new LigneTest("2", "100", "0", "20")));

        assertThat(t.totalHT()).isEqualByComparingTo("200.00");
        assertThat(t.totalTVA()).isEqualByComparingTo("40.00");
        assertThat(t.totalTTC()).isEqualByComparingTo("240.00");
    }

    @Test
    void remiseDeDixPourCent() {
        // 3 x 50 = 150 ; remise 10 % -> 135 HT ; TVA 20 % = 27
        Totaux t = calculateur.calculer(List.of(new LigneTest("3", "50", "10", "20")));

        assertThat(t.totalHT()).isEqualByComparingTo("135.00");
        assertThat(t.totalTVA()).isEqualByComparingTo("27.00");
        assertThat(t.totalTTC()).isEqualByComparingTo("162.00");
    }

    @Test
    void remiseAbsenteEstTraiteeCommeZero() {
        Totaux t = calculateur.calculer(List.of(new LigneTest("1", "100", null, "20")));

        assertThat(t.totalHT()).isEqualByComparingTo("100.00");
    }

    @Test
    void plusieursTauxDeTva() {
        // Ligne A : 100 HT à 20 % -> TVA 20 ; ligne B : 200 HT à 10 % -> TVA 20
        Totaux t = calculateur.calculer(List.of(
                new LigneTest("1", "100", "0", "20"),
                new LigneTest("1", "200", "0", "10")));

        assertThat(t.totalHT()).isEqualByComparingTo("300.00");
        assertThat(t.totalTVA()).isEqualByComparingTo("40.00");
        assertThat(t.totalTTC()).isEqualByComparingTo("340.00");
        assertThat(t.tvaParTaux()).hasSize(2);
        assertThat(t.tvaParTaux().get(new BigDecimal("20"))).isEqualByComparingTo("20.00");
        assertThat(t.tvaParTaux().get(new BigDecimal("10"))).isEqualByComparingTo("20.00");
    }

    @Test
    void arrondiHalfUp() {
        // 1 x 0.335 = 0.335 -> HT arrondi 0.34 ; TVA 20 % de 0.34 = 0.068 -> 0.07
        LigneTest ligne = new LigneTest("1", "0.335", "0", "20");

        assertThat(calculateur.montantHT(ligne)).isEqualByComparingTo("0.34");
        assertThat(calculateur.montantTVA(ligne)).isEqualByComparingTo("0.07");
    }

    @Test
    void listeVideDonneZero() {
        Totaux t = calculateur.calculer(List.of());

        assertThat(t.totalHT()).isEqualByComparingTo("0.00");
        assertThat(t.totalTTC()).isEqualByComparingTo("0.00");
        assertThat(t.tvaParTaux()).isEmpty();
    }
}
