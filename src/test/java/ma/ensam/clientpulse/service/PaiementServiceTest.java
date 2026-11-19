package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.enums.ModePaiement;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.impl.PaiementServiceImpl;
import ma.ensam.clientpulse.service.mapper.FactureMapper;
import ma.ensam.clientpulse.service.mapper.PaiementMapper;
import ma.ensam.clientpulse.service.memoire.Donnees;
import ma.ensam.clientpulse.service.memoire.FactureDaoMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaiementServiceTest {

    private FactureDaoMemoire factureDao;
    private PaiementServiceImpl service;
    private Facture facture;   // 1000 HT + 20 % = 1200 TTC

    @BeforeEach
    void preparer() {
        factureDao = new FactureDaoMemoire();
        PaiementMapper paiementMapper = new PaiementMapper();
        // Le PaiementDao (3e argument) n'est utilisé que par lister() : null suffit ici
        service = new PaiementServiceImpl(factureDao, null, paiementMapper, new FactureMapper(paiementMapper));
        facture = factureDao.save(Donnees.factureEmise(Donnees.client(1L), "1000",
                LocalDate.of(2026, 12, 1), "FAC-2026-0001"));
    }

    private PaiementDto paiement(String montant) {
        return new PaiementDto(null, null, LocalDate.of(2026, 11, 25), new BigDecimal(montant),
                ModePaiement.VIREMENT, "REF");
    }

    @Test
    void unPaiementPartielLaisseUnResteAPayer() {
        FactureDto resultat = service.enregistrer(facture.getId(), paiement("500.00"));

        assertThat(resultat.statut()).isEqualTo(StatutFacture.PARTIELLEMENT_PAYEE);
        assertThat(resultat.totalPaye()).isEqualByComparingTo("500.00");
        assertThat(resultat.resteAPayer()).isEqualByComparingTo("700.00");
        assertThat(resultat.paiements()).hasSize(1);
    }

    @Test
    void deuxPaiementsQuiSoldentLaFacture() {
        service.enregistrer(facture.getId(), paiement("500.00"));
        FactureDto resultat = service.enregistrer(facture.getId(), paiement("700.00"));

        assertThat(resultat.statut()).isEqualTo(StatutFacture.PAYEE);
        assertThat(resultat.resteAPayer()).isEqualByComparingTo("0.00");
        assertThat(resultat.paiements()).hasSize(2);
    }

    @Test
    void unPaiementSuperieurAuResteEstRefuse() {
        assertThatThrownBy(() -> service.enregistrer(facture.getId(), paiement("1200.01")))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("dépasse");
    }

    @Test
    void onNePaiePasUneFactureQuiNEstPasEmise() {
        Facture brouillon = factureDao.save(new Facture());

        assertThatThrownBy(() -> service.enregistrer(brouillon.getId(), paiement("10.00")))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void factureInconnue() {
        assertThatThrownBy(() -> service.enregistrer(999L, paiement("10.00")))
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}
