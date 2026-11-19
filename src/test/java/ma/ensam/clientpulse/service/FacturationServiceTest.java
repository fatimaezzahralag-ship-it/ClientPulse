package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Devis;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.LigneDevis;
import ma.ensam.clientpulse.domain.enums.StatutDevis;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.impl.CalculateurMontantsImpl;
import ma.ensam.clientpulse.service.impl.FacturationServiceImpl;
import ma.ensam.clientpulse.service.interfaces.NumerotationService;
import ma.ensam.clientpulse.service.mapper.FactureMapper;
import ma.ensam.clientpulse.service.mapper.PaiementMapper;
import ma.ensam.clientpulse.service.memoire.DevisDaoMemoire;
import ma.ensam.clientpulse.service.memoire.Donnees;
import ma.ensam.clientpulse.service.memoire.FactureDaoMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Teste FacturationServiceImpl avec de faux DAO en mémoire : aucun Spring, aucune base. */
class FacturationServiceTest {

    private FactureDaoMemoire factureDao;
    private DevisDaoMemoire devisDao;
    private FacturationServiceImpl service;
    private Client client;

    @BeforeEach
    void preparer() {
        factureDao = new FactureDaoMemoire();
        devisDao = new DevisDaoMemoire();
        client = Donnees.client(1L);

        AtomicInteger compteur = new AtomicInteger();
        NumerotationService numerotation = (type, jour) ->
                String.format("FAC-%d-%04d", jour.getYear(), compteur.incrementAndGet());

        service = new FacturationServiceImpl(factureDao, devisDao, new CalculateurMontantsImpl(),
                numerotation, new FactureMapper(new PaiementMapper()));
    }

    /** Un devis de 2 x 100 HT, TVA 20 % : 200 HT, 40 de TVA, 240 TTC. */
    private Devis devis(StatutDevis statut) {
        LigneDevis ligne = new LigneDevis();
        ligne.setDesignation("Licence");
        ligne.setQuantite(new BigDecimal("2"));
        ligne.setPrixUnitaireHT(new BigDecimal("100.00"));
        ligne.setRemise(BigDecimal.ZERO);
        ligne.setTauxTVA(new BigDecimal("20.00"));

        Devis d = new Devis();
        d.setClient(client);
        d.setStatut(statut);
        d.ajouterLigne(ligne);
        return devisDao.save(d);
    }

    @Test
    void creerDepuisDevisCopieLesLignesEtCalculeLesTotaux() {
        Devis devis = devis(StatutDevis.ACCEPTE);

        FactureDto facture = service.creerDepuisDevis(devis.getId());

        assertThat(facture.statut()).isEqualTo(StatutFacture.BROUILLON);
        assertThat(facture.numero()).isNull();
        assertThat(facture.devisId()).isEqualTo(devis.getId());
        assertThat(facture.lignes()).hasSize(1);
        assertThat(facture.totalHT()).isEqualByComparingTo("200.00");
        assertThat(facture.totalTVA()).isEqualByComparingTo("40.00");
        assertThat(facture.totalTTC()).isEqualByComparingTo("240.00");
        assertThat(facture.lignes().get(0).montantHT()).isEqualByComparingTo("200.00");
    }

    @Test
    void unDevisNonAccepteNePeutPasEtreFacture() {
        Devis brouillon = devis(StatutDevis.BROUILLON);

        assertThatThrownBy(() -> service.creerDepuisDevis(brouillon.getId()))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void unDevisNeSeFactureQuUneFois() {
        Devis devis = devis(StatutDevis.ACCEPTE);
        service.creerDepuisDevis(devis.getId());

        assertThatThrownBy(() -> service.creerDepuisDevis(devis.getId()))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("déjà été facturé");
    }

    @Test
    void emettreDonneLeNumeroEtUneEcheanceParDefautA30Jours() {
        FactureDto brouillon = service.creerDepuisDevis(devis(StatutDevis.ACCEPTE).getId());

        FactureDto emise = service.emettre(brouillon.id(), LocalDate.of(2026, 11, 20), null);

        assertThat(emise.numero()).isEqualTo("FAC-2026-0001");
        assertThat(emise.statut()).isEqualTo(StatutFacture.EMISE);
        assertThat(emise.dateEcheance()).isEqualTo(LocalDate.of(2026, 12, 20));
        assertThat(emise.resteAPayer()).isEqualByComparingTo("240.00");
    }

    @Test
    void uneEcheanceAvantLEmissionEstRefusee() {
        FactureDto brouillon = service.creerDepuisDevis(devis(StatutDevis.ACCEPTE).getId());

        assertThatThrownBy(() -> service.emettre(brouillon.id(),
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 1)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void onNeLaPasEmetDeuxFois() {
        FactureDto brouillon = service.creerDepuisDevis(devis(StatutDevis.ACCEPTE).getId());
        service.emettre(brouillon.id(), LocalDate.of(2026, 11, 20), null);

        assertThatThrownBy(() -> service.emettre(brouillon.id(), LocalDate.of(2026, 11, 21), null))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void annulerUneFactureEmiseSansPaiement() {
        FactureDto brouillon = service.creerDepuisDevis(devis(StatutDevis.ACCEPTE).getId());
        service.emettre(brouillon.id(), LocalDate.of(2026, 11, 20), null);

        FactureDto annulee = service.annuler(brouillon.id());

        assertThat(annulee.statut()).isEqualTo(StatutFacture.ANNULEE);
    }

    @Test
    void marquerEnRetardNeTouchePasLesFacturesNonEchues() {
        Facture echue = factureDao.save(
                Donnees.factureEmise(client, "1000", LocalDate.of(2026, 12, 1), "FAC-2026-0101"));
        Facture pasEchue = factureDao.save(
                Donnees.factureEmise(client, "1000", LocalDate.of(2027, 1, 1), "FAC-2026-0102"));

        int nombre = service.marquerEnRetard(LocalDate.of(2026, 12, 5));

        assertThat(nombre).isEqualTo(1);
        assertThat(echue.getStatut()).isEqualTo(StatutFacture.EN_RETARD);
        assertThat(pasEchue.getStatut()).isEqualTo(StatutFacture.EMISE);
    }

    @Test
    void uneFactureDejaEnRetardNEstPasRecomptee() {
        factureDao.save(Donnees.factureEmise(client, "1000", LocalDate.of(2026, 12, 1), "FAC-2026-0101"));

        assertThat(service.marquerEnRetard(LocalDate.of(2026, 12, 5))).isEqualTo(1);
        assertThat(service.marquerEnRetard(LocalDate.of(2026, 12, 6))).isEqualTo(0);
    }
}
