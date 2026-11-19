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
class ReclamationsFacteurTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 6, 30);

    @Mock
    private StatistiqueClientDao statistiqueClientDao;

    private ReclamationsFacteur facteur;
    private Client client;

    @BeforeEach
    void setUp() {
        facteur = new ReclamationsFacteur(
                statistiqueClientDao,
                new ParametresScoringReader(new ParametreScoringDaoStub())
        );
        client = new Client();
        client.setId(1L);
    }

    @Test
    void aucune_reclamation_ouverte_a_une_note_faible() {
        statsAvec(0L);

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals("RECLAMATIONS", resultat.code());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultat.note()));
        assertEquals("Aucune réclamation ouverte", resultat.explication());
    }

    @Test
    void plusieurs_reclamations_ouvertes_ont_une_note_elevee() {
        statsAvec(3L);

        ResultatFacteur resultat = facteur.evaluer(client, JOUR);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(resultat.note()));
    }

    private void statsAvec(Long reclamations) {
        when(statistiqueClientDao.findStatistiquesByClientId(1L, JOUR))
                .thenReturn(Optional.of(new StatistiqueClientDto(
                        1L,
                        JOUR.minusDays(15),
                        new BigDecimal("30000"),
                        reclamations,
                        BigDecimal.ZERO,
                        new BigDecimal("8000"),
                        new BigDecimal("12000")
                )));
    }
}
