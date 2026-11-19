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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaisseAchatsFacteurTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock
    private StatistiqueClientDao statistiqueClientDao;

    private BaisseAchatsFacteur facteur;
    private Client client;

    @BeforeEach
    void setUp() {
        facteur = new BaisseAchatsFacteur(
                statistiqueClientDao,
                new ParametresScoringReader(new ParametreScoringDaoStub())
        );
        client = new Client();
        client.setId(1L);
    }

    @Test
    void baisse_importante_des_achats_a_une_note_elevee() {
        // 25 000 -> 10 000 = 60 % de baisse
        statsAvec(new BigDecimal("10000"), new BigDecimal("25000"));

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals("BAISSE_ACHATS", resultat.code());
        assertEquals(0, BigDecimal.valueOf(70).compareTo(resultat.note()));
        assertTrue(resultat.explication().contains("60.0 %"));
    }

    @Test
    void achats_stables_ne_penalisent_pas_le_client() {
        statsAvec(new BigDecimal("25000"), new BigDecimal("25000"));

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals(0, BigDecimal.ZERO.compareTo(resultat.note()));
    }

    private void statsAvec(BigDecimal recent, BigDecimal precedent) {
        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        JOUR.minusDays(5),
                        recent.add(precedent),
                        0L,
                        BigDecimal.ZERO,
                        recent,
                        precedent
                )));
    }
}
