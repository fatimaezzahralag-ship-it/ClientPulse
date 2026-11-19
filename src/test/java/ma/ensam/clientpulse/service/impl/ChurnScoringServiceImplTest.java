package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.FacteurRisque;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.mapper.ScoreClientMapper;
import ma.ensam.clientpulse.service.scoring.NomsParametres;
import ma.ensam.clientpulse.service.scoring.ParametreScoringDaoStub;
import ma.ensam.clientpulse.service.scoring.ParametresScoringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChurnScoringServiceImplTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock private FacteurRisque facteurInactivite;
    @Mock private FacteurRisque facteurBaisse;
    @Mock private FacteurRisque facteurReclamations;
    @Mock private FacteurRisque facteurRetard;

    @Mock private ScoreClientDao scoreClientDao;
    @Mock private ClientDao clientDao;
    @Mock private StatistiqueClientDao statistiqueClientDao;

    private Client client;
    private ParametreScoringDaoStub parametres;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);

        parametres = new ParametreScoringDaoStub();

        when(scoreClientDao.save(any(ScoreClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        JOUR.minusDays(20),
                        new BigDecimal("60000"),
                        0L,
                        BigDecimal.ZERO,
                        new BigDecimal("20000"),
                        new BigDecimal("18000")
                )));
    }

    @Test
    void des_poids_differents_recalculent_le_score_global() {
        configurerNotes(0, 100, 100, 100);

        ChurnScoringServiceImpl service = serviceAvec(parametres);
        ScoreClient premier = service.calculer(client, JOUR);

        // 0.30*0 + 0.25*100 + 0.15*100 + 0.30*100 = 70
        assertEquals(0, new BigDecimal("70.00")
                .compareTo(premier.getScoreRisque()));

        ParametreScoringDaoStub autresPoids = new ParametreScoringDaoStub()
                .avec(NomsParametres.POIDS_INACTIVITE, "0.70")
                .avec(NomsParametres.POIDS_BAISSE_ACHATS, "0.10")
                .avec(NomsParametres.POIDS_RECLAMATIONS, "0.10")
                .avec(NomsParametres.POIDS_RETARD_PAIEMENT, "0.10");

        ScoreClient second = serviceAvec(autresPoids).calculer(client, JOUR);

        // 0.70*0 + 0.10*100 + 0.10*100 + 0.10*100 = 30
        assertEquals(0, new BigDecimal("30.00")
                .compareTo(second.getScoreRisque()));
        assertNotEquals(premier.getScoreRisque(), second.getScoreRisque());
    }

    @Test
    void un_score_au_dessus_du_seuil_eleve_donne_un_niveau_eleve() {
        // 0.30*70 + 0.25*100 + 0.15*70 + 0.30*90 = 83.50
        configurerNotes(70, 100, 70, 90);

        ScoreClient score = serviceAvec(parametres).calculer(client, JOUR);

        assertEquals(0, new BigDecimal("83.50")
                .compareTo(score.getScoreRisque()));
        assertEquals(NiveauRisque.ELEVE, score.getNiveau());
        assertEquals(4, score.getFacteursExplicatifs().lines().count());
        assertTrue(score.getFacteursExplicatifs().contains("INACTIVITE"));
    }

    @Test
    void un_score_tres_eleve_devient_critique() {
        configurerNotes(100, 100, 100, 100);

        ScoreClient score = serviceAvec(parametres).calculer(client, JOUR);

        assertEquals(0, new BigDecimal("100.00")
                .compareTo(score.getScoreRisque()));
        assertEquals(NiveauRisque.CRITIQUE, score.getNiveau());
    }

    private void configurerNotes(
            int inactivite,
            int baisse,
            int reclamations,
            int retard) {

        configurer(
                facteurInactivite, "INACTIVITE",
                BigDecimal.valueOf(inactivite));
        configurer(
                facteurBaisse, "BAISSE_ACHATS",
                BigDecimal.valueOf(baisse));
        configurer(
                facteurReclamations, "RECLAMATIONS",
                BigDecimal.valueOf(reclamations));
        configurer(
                facteurRetard, "RETARD_PAIEMENT",
                BigDecimal.valueOf(retard));
    }

    private void configurer(
            FacteurRisque facteur,
            String code,
            BigDecimal note) {

        when(facteur.evaluer(client, JOUR)).thenReturn(
                new ResultatFacteur(code, note, code + " : " + note));
    }

    private ChurnScoringServiceImpl serviceAvec(
            ParametreScoringDaoStub parametresScoring) {

        return new ChurnScoringServiceImpl(
                List.of(
                        facteurInactivite,
                        facteurBaisse,
                        facteurReclamations,
                        facteurRetard
                ),
                new ParametresScoringReader(parametresScoring),
                scoreClientDao,
                clientDao,
                statistiqueClientDao,
                new ScoreClientMapper()
        );
    }
}
