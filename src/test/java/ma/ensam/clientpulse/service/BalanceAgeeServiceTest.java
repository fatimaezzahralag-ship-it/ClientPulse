package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.dto.BalanceAgeeDto;
import ma.ensam.clientpulse.service.impl.BalanceAgeeServiceImpl;
import ma.ensam.clientpulse.service.memoire.Donnees;
import ma.ensam.clientpulse.service.memoire.FactureDaoMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceAgeeServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 12, 21);

    private FactureDaoMemoire factureDao;
    private BalanceAgeeServiceImpl service;

    @BeforeEach
    void preparer() {
        factureDao = new FactureDaoMemoire();
        service = new BalanceAgeeServiceImpl(factureDao);
    }

    @Test
    void classeLesCreancesParTrancheDeRetard() {
        // Client 1 : 1200 TTC échue depuis 20 jours, 600 TTC échue depuis 111 jours
        factureDao.save(Donnees.factureEmise(Donnees.client(1L), "1000", LocalDate.of(2026, 12, 1), "FAC-2026-0001"));
        factureDao.save(Donnees.factureEmise(Donnees.client(1L), "500", LocalDate.of(2026, 9, 1), "FAC-2026-0002"));
        // Client 2 : 120 TTC pas encore échue
        factureDao.save(Donnees.factureEmise(Donnees.client(2L), "100", LocalDate.of(2027, 1, 15), "FAC-2026-0003"));

        List<BalanceAgeeDto> balance = service.calculer(JOUR);

        assertThat(balance).hasSize(2);

        BalanceAgeeDto client1 = balance.get(0);
        assertThat(client1.clientId()).isEqualTo(1L);
        assertThat(client1.retard1a30()).isEqualByComparingTo("1200.00");
        assertThat(client1.retardPlusDe90()).isEqualByComparingTo("600.00");
        assertThat(client1.nonEchu()).isEqualByComparingTo("0");
        assertThat(client1.total()).isEqualByComparingTo("1800.00");

        BalanceAgeeDto client2 = balance.get(1);
        assertThat(client2.clientId()).isEqualTo(2L);
        assertThat(client2.nonEchu()).isEqualByComparingTo("120.00");
        assertThat(client2.total()).isEqualByComparingTo("120.00");
    }

    @Test
    void uneFacturePayeeNApparaitPas() {
        var facture = factureDao.save(
                Donnees.factureEmise(Donnees.client(1L), "1000", LocalDate.of(2026, 12, 1), "FAC-2026-0001"));
        var paiement = new ma.ensam.clientpulse.domain.entity.Paiement();
        paiement.setMontant(new java.math.BigDecimal("1200.00"));
        facture.enregistrerPaiement(paiement);

        assertThat(service.calculer(JOUR)).isEmpty();
    }

    @Test
    void sansFactureLaBalanceEstVide() {
        assertThat(service.calculer(JOUR)).isEmpty();
    }
}
