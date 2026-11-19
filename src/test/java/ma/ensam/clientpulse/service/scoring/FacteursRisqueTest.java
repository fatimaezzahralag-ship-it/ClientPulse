package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.facteur.BaisseAchatsFacteur;
import ma.ensam.clientpulse.service.facteur.InactiviteFacteur;
import ma.ensam.clientpulse.service.facteur.ReclamationsFacteur;
import ma.ensam.clientpulse.service.facteur.RetardPaiementFacteur;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Vue d'ensemble des quatre facteurs : chaque facteur est testé avec ses
 * seuils par défaut (migration V4), indépendamment des autres.
 */
@ExtendWith(MockitoExtension.class)
class FacteursRisqueTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock private StatistiqueClientDao statistiqueClientDao;

    private ParametresScoringReader parametres;
    private Client client;

    @BeforeEach
    void setUp() {
        parametres = new ParametresScoringReader(
                new ParametreScoringDaoStub());
        client = new Client();
        client.setId(1L);
    }

    @Test
    void inactivite_une_activite_recente_ne_penalise_pas() {
        stats(JOUR.minusDays(10), 0L, BigDecimal.ZERO,
                "10000", "20000");

        ResultatFacteur resultat = new InactiviteFacteur(
                statistiqueClientDao, parametres).evaluer(client, JOUR);

        assertEquals("INACTIVITE", resultat.code());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultat.note()));
    }

    @Test
    void inactivite_une_longue_absence_donne_la_note_maximale() {
        stats(JOUR.minusDays(200), 0L, BigDecimal.ZERO,
                "10000", "20000");

        ResultatFacteur resultat = new InactiviteFacteur(
                statistiqueClientDao, parametres).evaluer(client, JOUR);

        assertEquals(0, BigDecimal.valueOf(100)
                .compareTo(resultat.note()));
        assertTrue(resultat.explication().contains("200 jours"));
    }

    @Test
    void baisse_achats_une_chute_de_60_pourcent_donne_70() {
        stats(JOUR.minusDays(5), 0L, BigDecimal.ZERO,
                "10000", "25000");

        ResultatFacteur resultat = new BaisseAchatsFacteur(
                statistiqueClientDao, parametres).evaluer(client, JOUR);

        assertEquals("BAISSE_ACHATS", resultat.code());
        assertEquals(0, BigDecimal.valueOf(70)
                .compareTo(resultat.note()));
    }

    @Test
    void reclamations_trois_dossiers_ouverts_donnent_la_note_maximale() {
        stats(JOUR.minusDays(5), 3L, BigDecimal.ZERO,
                "10000", "20000");

        ResultatFacteur resultat = new ReclamationsFacteur(
                statistiqueClientDao, parametres).evaluer(client, JOUR);

        assertEquals("RECLAMATIONS", resultat.code());
        assertEquals(0, BigDecimal.valueOf(100)
                .compareTo(resultat.note()));
    }

    @Test
    void retard_un_retard_de_45_jours_donne_90() {
        stats(JOUR.minusDays(5), 0L, new BigDecimal("45"),
                "10000", "20000");

        ResultatFacteur resultat = new RetardPaiementFacteur(
                statistiqueClientDao, parametres).evaluer(client, JOUR);

        assertEquals("RETARD_PAIEMENT", resultat.code());
        assertEquals(0, BigDecimal.valueOf(90)
                .compareTo(resultat.note()));
    }

    private void stats(
            LocalDate derniereActivite,
            Long reclamations,
            BigDecimal retard,
            String recent,
            String precedent) {

        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        derniereActivite,
                        new BigDecimal(recent).add(new BigDecimal(precedent)),
                        reclamations,
                        retard,
                        new BigDecimal(recent),
                        new BigDecimal(precedent)
                )));
    }
}
