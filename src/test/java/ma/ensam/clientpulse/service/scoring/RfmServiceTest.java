package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.impl.RfmServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Les six segments RFM, avec les seuils par défaut de la migration V5. */
@ExtendWith(MockitoExtension.class)
class RfmServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock private StatistiqueClientDao statistiqueClientDao;
    @Mock private ClientDao clientDao;
    @Mock private ScoreClientDao scoreClientDao;

    private RfmServiceImpl service;
    private Client client;

    @BeforeEach
    void setUp() {
        service = new RfmServiceImpl(
                statistiqueClientDao,
                new ParametresScoringReader(new ParametreScoringDaoStub()),
                clientDao,
                scoreClientDao
        );
        client = new Client();
        client.setId(1L);
    }

    @Test
    void un_client_recent_frequent_et_important_est_champion() {
        // 5 jours, 15 achats, 80 000 DH.
        statsAvec(JOUR.minusDays(5), JOUR.minusDays(1000), 15L, "80000",
                JOUR.minusDays(5));

        assertEquals(SegmentRFM.CHAMPION, service.segmenter(client, JOUR));
        verify(statistiqueClientDao)
                .findStatistiquesByClientId(1L, JOUR);
    }

    @Test
    void un_client_regulier_mais_moins_important_est_loyal() {
        // 20 jours, 8 achats, 30 000 DH.
        statsAvec(JOUR.minusDays(20), JOUR.minusDays(1000), 8L, "30000",
                JOUR.minusDays(20));

        assertEquals(SegmentRFM.LOYAL, service.segmenter(client, JOUR));
    }

    @Test
    void un_client_recemment_acquis_est_nouveau() {
        // Premier achat il y a 20 jours : prioritaire sur les autres critères.
        statsAvec(JOUR.minusDays(10), JOUR.minusDays(20), 1L, "5000",
                JOUR.minusDays(10));

        assertEquals(SegmentRFM.NOUVEAU, service.segmenter(client, JOUR));
    }

    @Test
    void un_client_dont_l_activite_diminue_est_a_risque() {
        // 180 jours, 2 achats, 3 000 DH : encore relançable (seuil 240 jours).
        statsAvec(JOUR.minusDays(180), JOUR.minusDays(1000), 2L, "3000",
                JOUR.minusDays(180));

        assertEquals(SegmentRFM.A_RISQUE, service.segmenter(client, JOUR));
    }

    @Test
    void un_client_inactif_depuis_longtemps_est_en_hibernation() {
        statsAvec(JOUR.minusDays(300), JOUR.minusDays(1000), 6L, "20000",
                JOUR.minusDays(300));

        assertEquals(SegmentRFM.HIBERNATION, service.segmenter(client, JOUR));
    }

    @Test
    void un_client_sans_retour_depres_d_un_an_est_perdu() {
        statsAvec(JOUR.minusDays(400), JOUR.minusDays(1000), 1L, "1000",
                JOUR.minusDays(400));

        assertEquals(SegmentRFM.PERDU, service.segmenter(client, JOUR));
    }

    @Test
    void sans_achat_mais_avec_une_activite_recente_le_client_est_nouveau() {
        statsAvec(null, null, 0L, "0", JOUR.minusDays(10));

        assertEquals(SegmentRFM.NOUVEAU, service.segmenter(client, JOUR));
    }

    @Test
    void sans_achat_et_sans_activite_le_client_est_perdu() {
        statsAvec(null, null, 0L, "0", null);

        assertEquals(SegmentRFM.PERDU, service.segmenter(client, JOUR));
    }

    private void statsAvec(
            LocalDate dernierAchat,
            LocalDate premierAchat,
            Long nombreAchats,
            String montant,
            LocalDate derniereActivite) {

        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        derniereActivite,
                        new BigDecimal(montant),
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        dernierAchat,
                        premierAchat,
                        nombreAchats,
                        new BigDecimal(montant)
                )));
    }
}
