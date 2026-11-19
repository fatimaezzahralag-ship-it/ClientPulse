package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.RelanceRequest;
import ma.ensam.clientpulse.service.impl.RelanceCommercialServiceImpl;
import ma.ensam.clientpulse.service.interfaces.RelanceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Génération des relances : anti-doublon, nature et affectation. */
@ExtendWith(MockitoExtension.class)
class RelanceCommercialServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock private ScoreClientDao scoreClientDao;
    @Mock private RelanceService relanceService;

    private RelanceCommercialServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RelanceCommercialServiceImpl(
                scoreClientDao, relanceService);
    }

    @Test
    void cree_une_relance_commerciale_pour_un_client_a_risque() {
        scoreEleve();
        when(relanceService.relanceActive(1L, NatureRelance.COMMERCIALE))
                .thenReturn(Optional.empty());
        when(relanceService.creer(any(RelanceRequest.class)))
                .thenAnswer(invocation -> null);

        assertEquals(1, service.genererRelances(JOUR));

        ArgumentCaptor<RelanceRequest> captor =
                ArgumentCaptor.forClass(RelanceRequest.class);
        verify(relanceService).creer(captor.capture());

        RelanceRequest requete = captor.getValue();
        assertEquals(1L, requete.clientId());
        assertEquals(NatureRelance.COMMERCIALE, requete.nature());
        assertTrue(requete.motif().contains("ELEVE"));
        assertTrue(requete.motif().contains("A_RISQUE"));
        assertTrue(requete.motif().contains("Inactivite"));
    }

    @Test
    void ne_cree_pas_de_doublon_si_une_relance_est_deja_active() {
        scoreEleve();
        RelanceDto existante = org.mockito.Mockito.mock(RelanceDto.class);
        when(relanceService.relanceActive(1L, NatureRelance.COMMERCIALE))
                .thenReturn(Optional.of(existante));

        assertEquals(0, service.genererRelances(JOUR));

        verify(relanceService, never()).creer(any(RelanceRequest.class));
    }

    @Test
    void transmet_le_resultat_et_le_commentaire_au_contrat_commun() {
        service.traiterRelance(
                7L,
                ResultatRelance.RENDEZ_VOUS_PRIS,
                "Rendez-vous fixé au 12/06"
        );

        verify(relanceService).traiter(
                7L,
                ResultatRelance.RENDEZ_VOUS_PRIS,
                "Rendez-vous fixé au 12/06"
        );
    }

    private void scoreEleve() {
        Client client = new Client();
        client.setId(1L);

        ScoreClient score = new ScoreClient();
        score.setClient(client);
        score.setNiveau(NiveauRisque.ELEVE);
        score.setSegment(SegmentRFM.A_RISQUE);
        score.setScoreRisque(new BigDecimal("85"));
        score.setFacteursExplicatifs("Inactivite : 120 jours");

        when(scoreClientDao.findDerniersScoresARelancer())
                .thenReturn(List.of(score));
    }
}
