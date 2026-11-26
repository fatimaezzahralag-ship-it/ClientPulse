package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.Paiement;
import ma.ensam.clientpulse.domain.enums.ModePaiement;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.impl.CalculateurMontantsImpl;
import ma.ensam.clientpulse.service.impl.FacturationServiceImpl;
import ma.ensam.clientpulse.service.impl.RelancePaiementServiceImpl;
import ma.ensam.clientpulse.service.mapper.FactureMapper;
import ma.ensam.clientpulse.service.mapper.PaiementMapper;
import ma.ensam.clientpulse.service.memoire.DevisDaoMemoire;
import ma.ensam.clientpulse.service.memoire.Donnees;
import ma.ensam.clientpulse.service.memoire.FactureDaoMemoire;
import ma.ensam.clientpulse.service.memoire.RelanceDaoMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RelancePaiementServiceTest {

    private static final LocalDate ECHEANCE = LocalDate.of(2026, 12, 1);

    private FactureDaoMemoire factureDao;
    private RelanceDaoMemoire relanceDao;
    private RelancePaiementServiceImpl service;
    private Facture facture;   // 1000 HT + 20 % = 1200 TTC, échéance le 01/12/2026

    @BeforeEach
    void preparer() {
        factureDao = new FactureDaoMemoire();
        relanceDao = new RelanceDaoMemoire();
        FacturationServiceImpl facturation = new FacturationServiceImpl(factureDao, new DevisDaoMemoire(),
                new CalculateurMontantsImpl(), (type, jour) -> "N/A", new FactureMapper(new PaiementMapper()));
        service = new RelancePaiementServiceImpl(factureDao, relanceDao, facturation);

        Client client = Donnees.client(1L);
        facture = factureDao.save(Donnees.factureEmise(client, "1000", ECHEANCE, "FAC-2026-0001"));
    }

    @Test
    void aucuneRelanceAvantJMoins3() {
        int creees = service.genererRelances(ECHEANCE.minusDays(4));

        assertThat(creees).isEqualTo(0);
        assertThat(relanceDao.toutes()).isEmpty();
    }

    @Test
    void niveau1ATroisJoursAvantLEcheance() {
        int creees = service.genererRelances(ECHEANCE.minusDays(3));

        assertThat(creees).isEqualTo(1);
        var relance = relanceDao.toutes().get(0);
        assertThat(relance.getNiveau()).isEqualTo(1);
        assertThat(relance.getNature()).isEqualTo(NatureRelance.PAIEMENT);
        assertThat(relance.getStatut()).isEqualTo(StatutRelance.A_FAIRE);
        assertThat(relance.getMotif()).contains("J-3").contains("FAC-2026-0001").contains("1200.00");
    }

    @Test
    void unNiveauNEstCreeQuUneFoisMemeSiLeJobTourneChaqueNuit() {
        service.genererRelances(ECHEANCE.plusDays(15));
        service.genererRelances(ECHEANCE.plusDays(16));
        service.genererRelances(ECHEANCE.plusDays(17));

        assertThat(relanceDao.toutes()).hasSize(1);
        assertThat(relanceDao.toutes().get(0).getNiveau()).isEqualTo(3);
    }

    @Test
    void chaqueNouveauNiveauCreeUneNouvelleRelance() {
        service.genererRelances(ECHEANCE.minusDays(3));   // niveau 1
        service.genererRelances(ECHEANCE.plusDays(3));    // niveau 2
        service.genererRelances(ECHEANCE.plusDays(45));   // niveau 5

        assertThat(relanceDao.toutes()).hasSize(3);
    }

    @Test
    void uneFacturePayeeNEstPlusRelancee() {
        Paiement paiement = new Paiement();
        paiement.setDatePaiement(ECHEANCE);
        paiement.setMontant(new BigDecimal("1200.00"));
        paiement.setMode(ModePaiement.VIREMENT);
        facture.enregistrerPaiement(paiement);

        int creees = service.genererRelances(ECHEANCE.plusDays(20));

        assertThat(creees).isEqualTo(0);
    }

    @Test
    void traiterJourMetLaFactureEnRetardPuisCreeLaRelance() {
        ResultatTraitement bilan = service.traiterJour(ECHEANCE.plusDays(20));

        assertThat(bilan.facturesPasseesEnRetard()).isEqualTo(1);
        assertThat(bilan.relancesCreees()).isEqualTo(1);
        assertThat(facture.getStatut()).isEqualTo(StatutFacture.EN_RETARD);
    }

    @Test
    void lesDelaisParametresParLAdministrateurSontPrisEnCompte() {
        // Rappel préventif 7 jours avant l'échéance au lieu de 3
        var parametres = new ma.ensam.clientpulse.service.scoring.ParametreScoringDaoStub()
                .avec(ma.ensam.clientpulse.service.scoring.NomsParametres.DELAI_RELANCE_N1, "7");
        FacturationServiceImpl facturation = new FacturationServiceImpl(factureDao, new DevisDaoMemoire(),
                new CalculateurMontantsImpl(), (type, jour) -> "N/A", new FactureMapper(new PaiementMapper()));
        var serviceParametre = new RelancePaiementServiceImpl(factureDao, relanceDao, facturation,
                new ma.ensam.clientpulse.service.impl.DelaisRelance(parametres));

        assertThat(serviceParametre.genererRelances(ECHEANCE.minusDays(8))).isEqualTo(0);
        assertThat(serviceParametre.genererRelances(ECHEANCE.minusDays(7))).isEqualTo(1);
        assertThat(relanceDao.toutes().get(0).getMotif()).contains("J-7");
    }
}
