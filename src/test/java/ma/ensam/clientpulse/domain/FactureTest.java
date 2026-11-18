package ma.ensam.clientpulse.domain;

import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.LigneFacture;
import ma.ensam.clientpulse.domain.entity.Paiement;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Teste les règles écrites dans l'entité Facture. Aucun Spring, aucune base. */
class FactureTest {

    private Facture facture;

    @BeforeEach
    void preparerUneFactureEmiseDe1000() {
        facture = new Facture();
        facture.ajouterLigne(new LigneFacture());   // une ligne suffit pour pouvoir émettre
        facture.setTotalTTC(new BigDecimal("1000.00"));
        facture.setDateEcheance(LocalDate.of(2026, 12, 1));
        facture.emettre("FAC-2026-0001");
    }

    private Paiement paiement(String montant) {
        Paiement p = new Paiement();
        p.setMontant(new BigDecimal(montant));
        return p;
    }

    @Test
    void emettreDonneLeNumeroEtFigeLaFacture() {
        assertThat(facture.getNumero()).isEqualTo("FAC-2026-0001");
        assertThat(facture.getStatut()).isEqualTo(StatutFacture.EMISE);
        assertThat(facture.estModifiable()).isFalse();
    }

    @Test
    void onNePeutPasEmettreDeuxFois() {
        assertThatThrownBy(() -> facture.emettre("FAC-2026-0002"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paiementPartielLaisseUnResteAPayer() {
        facture.enregistrerPaiement(paiement("400.00"));

        assertThat(facture.getStatut()).isEqualTo(StatutFacture.PARTIELLEMENT_PAYEE);
        assertThat(facture.getResteAPayer()).isEqualByComparingTo("600.00");
    }

    @Test
    void deuxPaiementsQuiSoldentLaFacture() {
        facture.enregistrerPaiement(paiement("400.00"));
        facture.enregistrerPaiement(paiement("600.00"));

        assertThat(facture.getStatut()).isEqualTo(StatutFacture.PAYEE);
        assertThat(facture.getResteAPayer()).isEqualByComparingTo("0.00");
    }

    @Test
    void paiementSuperieurAuResteEstRefuse() {
        assertThatThrownBy(() -> facture.enregistrerPaiement(paiement("1000.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void estEnRetardSeulementApresLEcheanceEtSiImpayee() {
        assertThat(facture.estEnRetard(LocalDate.of(2026, 12, 1))).isFalse(); // le jour même
        assertThat(facture.estEnRetard(LocalDate.of(2026, 12, 2))).isTrue();  // lendemain

        facture.enregistrerPaiement(paiement("1000.00"));
        assertThat(facture.estEnRetard(LocalDate.of(2026, 12, 2))).isFalse(); // payée
    }

    @Test
    void annulerRefuseSiDesPaiementsExistent() {
        facture.enregistrerPaiement(paiement("100.00"));

        assertThatThrownBy(() -> facture.annuler()).isInstanceOf(IllegalStateException.class);
    }
}
