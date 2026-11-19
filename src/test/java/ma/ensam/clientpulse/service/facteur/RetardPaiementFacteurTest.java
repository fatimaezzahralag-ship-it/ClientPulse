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
class RetardPaiementFacteurTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock
    private StatistiqueClientDao statistiqueClientDao;

    private RetardPaiementFacteur facteur;
    private Client client;

    @BeforeEach
    void setUp() {
        facteur = new RetardPaiementFacteur(
                statistiqueClientDao,
                new ParametresScoringReader(new ParametreScoringDaoStub())
        );
        client = new Client();
        client.setId(1L);
    }

    @Test
    void paiements_a_temps_donnent_une_note_faible() {
        statsAvec(BigDecimal.ZERO);

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals("RETARD_PAIEMENT", resultat.code());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultat.note()));
    }

    @Test
    void retards_frequents_donnent_une_note_elevee() {
        statsAvec(new BigDecimal("45"));

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals(0, BigDecimal.valueOf(90).compareTo(resultat.note()));
    }

    @Test
    void retard_modere_reste_dans_la_moyenne() {
        statsAvec(new BigDecimal("5"));

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals(0, BigDecimal.valueOf(20).compareTo(resultat.note()));
    }

    private void statsAvec(BigDecimal retardMoyen) {
        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        JOUR.minusDays(2),
                        new BigDecimal("40000"),
                        0L,
                        retardMoyen,
                        new BigDecimal("15000"),
                        new BigDecimal("20000")
                )));
    }
}
