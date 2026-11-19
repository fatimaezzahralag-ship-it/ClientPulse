package ma.ensam.clientpulse.service.facteur;

import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.scoring.ParametreScoringDaoStub;
import ma.ensam.clientpulse.service.scoring.ParametresScoringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InactiviteFacteurTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock
    private StatistiqueClientDao statistiqueClientDao;

    private InactiviteFacteur facteur;
    private Client client;

    @BeforeEach
    void setUp() {
        facteur = new InactiviteFacteur(
                statistiqueClientDao,
                new ParametresScoringReader(new ParametreScoringDaoStub())
        );
        client = new Client();
        client.setId(1L);
    }

    @Test
    void client_actif_recemment_a_une_note_d_inactivite_faible() {
        statsAvec(JOUR.minusDays(10));

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals("INACTIVITE", resultat.code());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultat.note()));
    }

    @Test
    void client_sans_activite_a_une_note_d_inactivite_elevee() {
        statsSansActivite();

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals("INACTIVITE", resultat.code());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(resultat.note()));
        assertEquals(
                "Aucune activité enregistrée pour ce client",
                resultat.explication()
        );
    }

    private void statsAvec(LocalDate derniereActivite) {
        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        derniereActivite,
                        new BigDecimal("50000"),
                        0L,
                        BigDecimal.ZERO,
                        new BigDecimal("10000"),
                        new BigDecimal("20000")
                )));
    }

    private void statsSansActivite() {
        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        null,
                        BigDecimal.ZERO,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )));
    }
}
