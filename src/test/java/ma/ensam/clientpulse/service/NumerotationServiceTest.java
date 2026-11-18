package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.dao.interfaces.SequenceNumerotationDao;
import ma.ensam.clientpulse.domain.entity.SequenceNumerotation;
import ma.ensam.clientpulse.domain.enums.TypeDocument;
import ma.ensam.clientpulse.service.impl.NumerotationServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * On remplace le DAO par un "mock" (faux objet) : pas besoin de base de données.
 * C'est possible parce que le service dépend d'une INTERFACE (couplage faible).
 * Le vrai test de verrouillage (deux utilisateurs en même temps) demande MySQL : voir README.
 */
class NumerotationServiceTest {

    private final SequenceNumerotationDao dao = mock(SequenceNumerotationDao.class);
    private final NumerotationServiceImpl service = new NumerotationServiceImpl(dao);

    @Test
    void premierNumeroDeLAnnee() {
        when(dao.verrouiller(TypeDocument.FACTURE, 2026))
                .thenReturn(new SequenceNumerotation(TypeDocument.FACTURE, 2026, 0));

        assertThat(service.prochainNumero(TypeDocument.FACTURE, LocalDate.of(2026, 3, 15)))
                .isEqualTo("FAC-2026-0001");
    }

    @Test
    void numeroSuivantApresLe41() {
        when(dao.verrouiller(TypeDocument.FACTURE, 2026))
                .thenReturn(new SequenceNumerotation(TypeDocument.FACTURE, 2026, 41));

        assertThat(service.prochainNumero(TypeDocument.FACTURE, LocalDate.of(2026, 11, 20)))
                .isEqualTo("FAC-2026-0042");
    }

    @Test
    void lePrefixeDependDuTypeDeDocument() {
        when(dao.verrouiller(TypeDocument.DEVIS, 2026))
                .thenReturn(new SequenceNumerotation(TypeDocument.DEVIS, 2026, 11));
        when(dao.verrouiller(TypeDocument.AVOIR, 2026))
                .thenReturn(new SequenceNumerotation(TypeDocument.AVOIR, 2026, 2));
        LocalDate jour = LocalDate.of(2026, 5, 1);

        assertThat(service.prochainNumero(TypeDocument.DEVIS, jour)).isEqualTo("DEV-2026-0012");
        assertThat(service.prochainNumero(TypeDocument.AVOIR, jour)).isEqualTo("AV-2026-0003");
    }

    @Test
    void leCompteurAugmenteEstSauvegarde() {
        SequenceNumerotation sequence = new SequenceNumerotation(TypeDocument.FACTURE, 2026, 9);
        when(dao.verrouiller(TypeDocument.FACTURE, 2026)).thenReturn(sequence);

        service.prochainNumero(TypeDocument.FACTURE, LocalDate.of(2026, 1, 2));

        assertThat(sequence.getDernierNumero()).isEqualTo(10);
        verify(dao).save(any(SequenceNumerotation.class));
    }

    @Test
    void chaqueAnneeRecommenceA1() {
        when(dao.verrouiller(TypeDocument.FACTURE, 2027))
                .thenReturn(new SequenceNumerotation(TypeDocument.FACTURE, 2027, 0));

        assertThat(service.prochainNumero(TypeDocument.FACTURE, LocalDate.of(2027, 1, 3)))
                .isEqualTo("FAC-2027-0001");
    }
}
