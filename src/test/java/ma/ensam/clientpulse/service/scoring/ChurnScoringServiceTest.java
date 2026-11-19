package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;
import ma.ensam.clientpulse.service.FacteurRisque;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.facteur.BaisseAchatsFacteur;
import ma.ensam.clientpulse.service.facteur.InactiviteFacteur;
import ma.ensam.clientpulse.service.facteur.ReclamationsFacteur;
import ma.ensam.clientpulse.service.facteur.RetardPaiementFacteur;
import ma.ensam.clientpulse.service.impl.ChurnScoringServiceImpl;
import ma.ensam.clientpulse.service.interfaces.RfmService;
import ma.ensam.clientpulse.service.mapper.ScoreClientMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests du moteur de scoring : poids, niveaux, enregistrement et paramètres. */
@ExtendWith(MockitoExtension.class)
class ChurnScoringServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock private FacteurRisque facteurInactivite;
    @Mock private FacteurRisque facteurBaisse;
    @Mock private FacteurRisque facteurReclamations;
    @Mock private FacteurRisque facteurRetard;

    @Mock private ScoreClientDao scoreClientDao;
    @Mock private ClientDao clientDao;
    @Mock private RfmService rfmService;

    private Client client;
    private ParametreScoringDaoStub parametres;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        parametres = new ParametreScoringDaoStub();

        // Certains scénarios échouent avant la sauvegarde : ces stubs sont
        // volontairement tolérés (aucun appel à ce moment-là).
        lenient().when(scoreClientDao.save(any(ScoreClient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(rfmService.segmenter(client, JOUR))
                .thenReturn(SegmentRFM.A_RISQUE);
    }

    @Test
    void calcule_le_score_avec_les_poids_de_la_base() {
        configurerNotes(0, 100, 100, 100);

        ScoreClient premier = serviceAvec(parametres).calculer(client, JOUR);

        // 0.30*0 + 0.25*100 + 0.15*100 + 0.30*100 = 70
        assertEquals(0, new BigDecimal("70.00")
                .compareTo(premier.getScoreRisque()));
        assertEquals(NiveauRisque.ELEVE, premier.getNiveau());
    }

    @Test
    void des_poids_differents_recalculent_le_score_global() {
        configurerNotes(0, 100, 100, 100);

        ScoreClient premier = serviceAvec(parametres).calculer(client, JOUR);

        ParametreScoringDaoStub autresPoids = new ParametreScoringDaoStub()
                .avec(NomsParametres.POIDS_INACTIVITE, "0.70")
                .avec(NomsParametres.POIDS_BAISSE_ACHATS, "0.10")
                .avec(NomsParametres.POIDS_RECLAMATIONS, "0.10")
                .avec(NomsParametres.POIDS_RETARD_PAIEMENT, "0.10");

        ScoreClient second = serviceAvec(autresPoids).calculer(client, JOUR);

        // 0.70*0 + 0.10*100 + 0.10*100 + 0.10*100 = 30
        assertEquals(0, new BigDecimal("30.00")
                .compareTo(second.getScoreRisque()));
        assertEquals(NiveauRisque.FAIBLE, second.getNiveau());
        assertNotEquals(premier.getScoreRisque(), second.getScoreRisque());
    }

    @Test
    void le_niveau_est_critique_au_dessus_du_dernier_seuil() {
        configurerNotes(100, 100, 100, 100);

        ScoreClient score = serviceAvec(parametres).calculer(client, JOUR);

        assertEquals(0, new BigDecimal("100.00")
                .compareTo(score.getScoreRisque()));
        assertEquals(NiveauRisque.CRITIQUE, score.getNiveau());
    }

    @Test
    void enregistre_le_score_avec_le_segment_fourni_par_le_rfm() {
        configurerNotes(70, 100, 70, 90);

        ScoreClient score = serviceAvec(parametres).calculer(client, JOUR);

        ArgumentCaptor<ScoreClient> captor =
                ArgumentCaptor.forClass(ScoreClient.class);
        verify(scoreClientDao).save(captor.capture());

        ScoreClient enregistre = captor.getValue();
        assertEquals(client, enregistre.getClient());
        assertEquals(JOUR, enregistre.getDateCalcul());
        assertEquals(SegmentRFM.A_RISQUE, enregistre.getSegment());
        assertEquals(4, enregistre.getFacteursExplicatifs().lines().count());
        assertTrue(enregistre.getFacteursExplicatifs().contains("INACTIVITE"));
        assertEquals(0, score.getScoreRisque()
                .compareTo(enregistre.getScoreRisque()));
    }

    @Test
    void un_parametre_absent_fait_echouer_le_calcul_sans_valeur_par_defaut() {
        configurerNotes(0, 0, 0, 0);
        ParametreScoringDaoStub incomplet = new ParametreScoringDaoStub()
                .sans(NomsParametres.POIDS_INACTIVITE);

        assertThrows(IllegalStateException.class,
                () -> serviceAvec(incomplet).calculer(client, JOUR));
    }

    private void configurerNotes(
            int inactivite, int baisse, int reclamations, int retard) {

        configurer(facteurInactivite, "INACTIVITE",
                BigDecimal.valueOf(inactivite));
        configurer(facteurBaisse, "BAISSE_ACHATS",
                BigDecimal.valueOf(baisse));
        configurer(facteurReclamations, "RECLAMATIONS",
                BigDecimal.valueOf(reclamations));
        configurer(facteurRetard, "RETARD_PAIEMENT",
                BigDecimal.valueOf(retard));
    }

    private void configurer(
            FacteurRisque facteur, String code, BigDecimal note) {

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
                rfmService,
                new ScoreClientMapper()
        );
    }
}
