package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.dao.interfaces.ProduitDao;
import ma.ensam.clientpulse.dao.interfaces.ReferencesExternesDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.entity.Produit;
import ma.ensam.clientpulse.domain.entity.TauxTVA;
import ma.ensam.clientpulse.domain.enums.StatutDevis;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.impl.CalculateurMontantsImpl;
import ma.ensam.clientpulse.service.impl.DevisServiceImpl;
import ma.ensam.clientpulse.service.mapper.DevisMapper;
import ma.ensam.clientpulse.service.memoire.DevisDaoMemoire;
import ma.ensam.clientpulse.service.memoire.Donnees;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevisServiceTest {

    private DevisServiceImpl service;
    private boolean clientExiste;

    @BeforeEach
    void preparer() {
        clientExiste = true;
        Client client = Donnees.client(1L);

        // Un produit du catalogue : "Licence", 100 HT, TVA 20 %
        TauxTVA tva20 = new TauxTVA();
        tva20.setId(1L);
        tva20.setTaux(new BigDecimal("20.00"));
        Produit licence = new Produit();
        licence.setId(5L);
        licence.setReference("LIC-1");
        licence.setDesignation("Licence");
        licence.setPrixUnitaireHT(new BigDecimal("100.00"));
        licence.setTauxTVA(tva20);

        ProduitDao produitDao = new ProduitDao() {
            @Override
            public Optional<Produit> findById(Long id) {
                return id.equals(5L) ? Optional.of(licence) : Optional.empty();
            }

            @Override
            public Produit save(Produit p) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<Produit> findByReference(String reference) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Page<Produit> findAll(Pageable pageable) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Page<Produit> rechercher(String motCle, Pageable pageable) {
                throw new UnsupportedOperationException();
            }
        };

        ReferencesExternesDao references = new ReferencesExternesDao() {
            @Override
            public Optional<Client> trouverClient(Long id) {
                return clientExiste ? Optional.of(client) : Optional.empty();
            }

            @Override
            public Optional<Commercial> trouverCommercial(Long id) {
                return Optional.empty();
            }
        };

        AtomicInteger compteur = new AtomicInteger();
        service = new DevisServiceImpl(new DevisDaoMemoire(), produitDao, references,
                new CalculateurMontantsImpl(),
                (type, jour) -> String.format("DEV-%d-%04d", jour.getYear(), compteur.incrementAndGet()),
                new DevisMapper(new CalculateurMontantsImpl()));
    }

    private DevisDto demande(LocalDate emission, LocalDate validite, LigneDto... lignes) {
        return new DevisDto(null, null, emission, validite, null, 1L, null, List.of(lignes), null, null, null);
    }

    /** 10 licences, remise 10 % : 900 HT, TVA 20 % = 180. Désignation, prix et TVA viennent du catalogue. */
    private LigneDto ligneProduit() {
        return new LigneDto(null, 5L, null, new BigDecimal("10"), null, new BigDecimal("10"), null, null, null);
    }

    /** Ligne libre : 2 x 500 HT, TVA 10 % = 100. */
    private LigneDto ligneLibre() {
        return new LigneDto(null, null, "Formation", new BigDecimal("2"), new BigDecimal("500.00"),
                null, new BigDecimal("10.00"), null, null);
    }

    @Test
    void creerCalculeLeNumeroEtLesTotaux() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneProduit(), ligneLibre()));

        assertThat(devis.numero()).isEqualTo("DEV-2026-0001");
        assertThat(devis.statut()).isEqualTo(StatutDevis.BROUILLON);
        assertThat(devis.totalHT()).isEqualByComparingTo("1900.00");
        assertThat(devis.totalTVA()).isEqualByComparingTo("280.00");
        assertThat(devis.totalTTC()).isEqualByComparingTo("2180.00");
    }

    @Test
    void uneLigneProduitReprendLeCatalogue() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneProduit()));

        LigneDto ligne = devis.lignes().get(0);
        assertThat(ligne.designation()).isEqualTo("Licence");
        assertThat(ligne.prixUnitaireHT()).isEqualByComparingTo("100.00");
        assertThat(ligne.tauxTVA()).isEqualByComparingTo("20.00");
        assertThat(ligne.montantHT()).isEqualByComparingTo("900.00");
    }

    @Test
    void uneLigneSansProduitDoitEtreComplete() {
        LigneDto incomplete = new LigneDto(null, null, null, new BigDecimal("1"), null, null, null, null, null);

        assertThatThrownBy(() -> service.creer(demande(LocalDate.of(2026, 11, 18), null, incomplete)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void clientInconnu() {
        clientExiste = false;

        assertThatThrownBy(() -> service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneLibre())))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void unDevisEnvoyeNEstPlusModifiable() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneLibre()));
        service.envoyer(devis.id());

        assertThatThrownBy(() -> service.modifier(devis.id(), demande(null, null, ligneLibre())))
                .isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> service.supprimer(devis.id()))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void modifierUnBrouillonRemplaceLesLignesEtRecalcule() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneProduit(), ligneLibre()));

        DevisDto modifie = service.modifier(devis.id(), demande(null, null, ligneLibre()));

        assertThat(modifie.lignes()).hasSize(1);
        assertThat(modifie.totalHT()).isEqualByComparingTo("1000.00");
        assertThat(modifie.totalTTC()).isEqualByComparingTo("1100.00");
    }

    @Test
    void cycleDeVieEnvoyerPuisAccepter() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneLibre()));

        assertThat(service.envoyer(devis.id()).statut()).isEqualTo(StatutDevis.ENVOYE);
        assertThat(service.accepter(devis.id()).statut()).isEqualTo(StatutDevis.ACCEPTE);
        assertThatThrownBy(() -> service.refuser(devis.id())).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void onNePeutPasAccepterUnBrouillon() {
        DevisDto devis = service.creer(demande(LocalDate.of(2026, 11, 18), null, ligneLibre()));

        assertThatThrownBy(() -> service.accepter(devis.id())).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void unDevisExpireNePeutPasEtreAccepte() {
        DevisDto devis = service.creer(
                demande(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1), ligneLibre()));
        service.envoyer(devis.id());

        assertThatThrownBy(() -> service.accepter(devis.id()))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("expiré");
    }

    @Test
    void laValiditeNePeutPasPrecederLEmission() {
        assertThatThrownBy(() -> service.creer(
                demande(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 1), ligneLibre())))
                .isInstanceOf(RegleMetierException.class);
    }
}
