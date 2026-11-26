package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.dao.interfaces.IndicateurDao;
import ma.ensam.clientpulse.dto.BalanceAgeeDto;
import ma.ensam.clientpulse.dto.ClientCaDto;
import ma.ensam.clientpulse.dto.DashboardDto;
import ma.ensam.clientpulse.dto.PointMensuelDto;
import ma.ensam.clientpulse.dto.SuccesRelancesDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.impl.DashboardServiceImpl;
import ma.ensam.clientpulse.service.interfaces.AnalyticsService;
import ma.ensam.clientpulse.service.interfaces.BalanceAgeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 11, 19);
    /** Fin du mois qui précède la période de 12 mois : décembre 2025 à novembre 2026. */
    private static final LocalDate DEBUT_12_MOIS = LocalDate.of(2025, 11, 30);

    private IndicateurFictif dao;
    private BalanceAgeeService balance;
    private AnalyticsService analytics;
    private DashboardServiceImpl service;

    @BeforeEach
    void preparer() {
        dao = new IndicateurFictif();
        balance = mock(BalanceAgeeService.class);
        analytics = mock(AnalyticsService.class);
        when(balance.calculer(JOUR)).thenReturn(List.of(
                new BalanceAgeeDto(1L, bd("600"), bd("200"), bd("0"), bd("0"), bd("0")),
                new BalanceAgeeDto(2L, bd("0"), bd("0"), bd("100"), bd("50"), bd("50"))));
        when(analytics.repartitionSegments()).thenReturn(Map.of("CHAMPION", 3L));
        when(analytics.repartitionNiveaux()).thenReturn(Map.of("FAIBLE", 5L));
        service = new DashboardServiceImpl(dao, balance, analytics);
    }

    @Test
    void calculeRetentionChurnEtClv() {
        dao.actifsDebut = 10;
        dao.actifsFin = 12;
        dao.nouveaux = 4;
        dao.marge = bd("100");
        dao.factures = 24;

        DashboardDto d = service.calculer(12, JOUR);

        // rétention = (12 - 4) / 10 = 80 % ; churn = 20 % ; durée de vie = 1 / 0,2 = 5 ans
        assertThat(d.clientsDebutPeriode()).isEqualTo(10);
        assertThat(d.clientsFinPeriode()).isEqualTo(12);
        assertThat(d.nouveauxClients()).isEqualTo(4);
        assertThat(d.tauxRetention()).isEqualByComparingTo("80.00");
        assertThat(d.tauxChurnAnnuel()).isEqualByComparingTo("20.00");
        assertThat(d.dureeVieAnnees()).isEqualByComparingTo("5.00");
        // 24 factures / 12 clients actifs = 2 factures par client et par an
        assertThat(d.facturesParClientParAn()).isEqualByComparingTo("2.00");
        // CLV = 100 x 2 x 5
        assertThat(d.clv()).isEqualByComparingTo("1000.00");
    }

    @Test
    void sansChurnLaDureeDeVieEstPlafonnee() {
        dao.actifsDebut = 10;
        dao.actifsFin = 10;
        dao.nouveaux = 0;
        dao.marge = bd("50");
        dao.factures = 10;

        DashboardDto d = service.calculer(12, JOUR);

        assertThat(d.tauxRetention()).isEqualByComparingTo("100.00");
        assertThat(d.dureeVieAnnees()).isEqualByComparingTo("10.00");
        assertThat(d.clv()).isEqualByComparingTo("500.00");   // 50 x 1 x 10
    }

    @Test
    void sansClientEnDebutDePeriodeLaRetentionEstNulleSansDivisionParZero() {
        dao.actifsDebut = 0;
        dao.actifsFin = 3;
        dao.nouveaux = 3;

        DashboardDto d = service.calculer(12, JOUR);

        assertThat(d.tauxRetention()).isEqualByComparingTo("0");
        assertThat(d.clv()).isEqualByComparingTo("0");
    }

    @Test
    void laRetentionNeDepassePasCentPourCentQuandDesClientsReviennent() {
        dao.actifsDebut = 5;
        dao.actifsFin = 12;   // des clients réactivés
        dao.nouveaux = 2;

        assertThat(service.calculer(12, JOUR).tauxRetention()).isEqualByComparingTo("100.00");
    }

    @Test
    void reprendLEncoursEtLesTranchesDeLaBalanceAgee() {
        DashboardDto d = service.calculer(12, JOUR);

        assertThat(d.encours()).isEqualByComparingTo("1000.00");
        assertThat(d.encoursEchu()).isEqualByComparingTo("400.00");
        assertThat(d.encoursParTranche()).hasSize(5);
        assertThat(d.encoursParTranche().get(0).tranche()).isEqualTo("Non échu");
        assertThat(d.encoursParTranche().get(0).montant()).isEqualByComparingTo("600.00");
        assertThat(d.encoursParTranche().get(0).pourcentage()).isEqualByComparingTo("60.00");
        assertThat(d.encoursParTranche().get(4).pourcentage()).isEqualByComparingTo("5.00");
    }

    @Test
    void calculeRecouvrementEtSuccesDesRelances() {
        dao.echu = bd("200000");
        dao.encaisseSurEchu = bd("150000");
        dao.relances = new SuccesRelancesDto(20, 5);
        dao.delai = bd("37.46");

        DashboardDto d = service.calculer(12, JOUR);

        assertThat(d.tauxRecouvrement()).isEqualByComparingTo("75.00");
        assertThat(d.tauxSuccesRelances()).isEqualByComparingTo("25.00");
        assertThat(d.relancesEffectuees()).isEqualTo(20);
        assertThat(d.relancesReussies()).isEqualTo(5);
        assertThat(d.dso()).isEqualByComparingTo("37.5");
    }

    @Test
    void laSerieMensuelleContientTousLesMoisMemeSansActivite() {
        dao.facturation = List.of(
                new PointMensuelDto("2026-01", bd("1000"), bd("0")),
                new PointMensuelDto("2026-11", bd("500"), bd("0")));
        dao.encaissement = List.of(new PointMensuelDto("2026-01", bd("0"), bd("800")));

        DashboardDto d = service.calculer(12, JOUR);

        assertThat(d.serieMensuelle()).hasSize(12);
        assertThat(d.serieMensuelle().get(0).mois()).isEqualTo("2025-12");
        assertThat(d.serieMensuelle().get(11).mois()).isEqualTo("2026-11");
        PointMensuelDto janvier = d.serieMensuelle().get(1);
        assertThat(janvier.mois()).isEqualTo("2026-01");
        assertThat(janvier.facture()).isEqualByComparingTo("1000.00");
        assertThat(janvier.encaisse()).isEqualByComparingTo("800.00");
        assertThat(d.serieMensuelle().get(5).facture()).isEqualByComparingTo("0");
    }

    @Test
    void uneCourtePeriodeCommenceAuBonMois() {
        DashboardDto d = service.calculer(3, JOUR);

        assertThat(d.periodeMois()).isEqualTo(3);
        assertThat(d.serieMensuelle()).extracting(PointMensuelDto::mois)
                .containsExactly("2026-09", "2026-10", "2026-11");
    }

    @Test
    void refuseUnePeriodeHorsDeLIntervalle() {
        assertThatThrownBy(() -> service.calculer(0, JOUR)).isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> service.calculer(13, JOUR)).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void exporteLesIndicateursEnCsvAvecBomEtSeparateurPointVirgule() {
        dao.actifsDebut = 10;
        dao.actifsFin = 12;
        dao.nouveaux = 4;

        String csv = service.exporterCsv(12, JOUR);

        assertThat(csv).startsWith("\uFEFF\"Indicateur\";\"Valeur\";\"Unité\"");
        assertThat(csv).contains("\"Taux de rétention\";\"80.00\";\"%\"");
        assertThat(csv).contains("\"Mois\";\"Facturé TTC (MAD)\";\"Encaissé (MAD)\"");
        assertThat(csv).contains("\"Plus de 90 j\"");
    }

    // ------------------------------------------------------------------ doublure du DAO

    private static BigDecimal bd(String valeur) {
        return new BigDecimal(valeur);
    }

    /** Doublure : renvoie les valeurs préparées par chaque test (zéro par défaut). */
    private static final class IndicateurFictif implements IndicateurDao {

        long actifsDebut;
        long actifsFin;
        long nouveaux;
        long factures;
        BigDecimal marge = BigDecimal.ZERO;
        BigDecimal echu = BigDecimal.ZERO;
        BigDecimal encaisseSurEchu = BigDecimal.ZERO;
        BigDecimal delai = BigDecimal.ZERO;
        SuccesRelancesDto relances = new SuccesRelancesDto(0, 0);
        List<PointMensuelDto> facturation = List.of();
        List<PointMensuelDto> encaissement = List.of();

        @Override
        public long compterClientsActifs(LocalDate date) {
            return date.equals(DEBUT_12_MOIS) ? actifsDebut : actifsFin;
        }

        @Override
        public long compterNouveauxClients(LocalDate debutExclu, LocalDate finInclus) {
            return nouveaux;
        }

        @Override
        public long compterFacturesEmises(LocalDate debutExclu, LocalDate finInclus) {
            return factures;
        }

        @Override
        public BigDecimal margeMoyenneParFacture(LocalDate debutExclu, LocalDate finInclus) {
            return marge;
        }

        @Override
        public BigDecimal chiffreAffairesHt(LocalDate debutExclu, LocalDate finInclus) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal totalFactureTtc(LocalDate debutExclu, LocalDate finInclus) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal totalEncaisse(LocalDate debutExclu, LocalDate finInclus) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal delaiMoyenPaiement(LocalDate debutExclu, LocalDate finInclus) {
            return delai;
        }

        @Override
        public BigDecimal montantFactureEcheance(LocalDate debutExclu, LocalDate finInclus) {
            return echu;
        }

        @Override
        public BigDecimal encaisseSurFacturesEcheance(LocalDate debutExclu, LocalDate finInclus) {
            return encaisseSurEchu;
        }

        @Override
        public SuccesRelancesDto succesRelances(LocalDate debutExclu, LocalDate finInclus) {
            return relances;
        }

        @Override
        public List<PointMensuelDto> facturationMensuelle(LocalDate debutExclu, LocalDate finInclus) {
            return facturation;
        }

        @Override
        public List<PointMensuelDto> encaissementMensuel(LocalDate debutExclu, LocalDate finInclus) {
            return encaissement;
        }

        @Override
        public List<ClientCaDto> meilleursClients(LocalDate debutExclu, LocalDate finInclus, int limite) {
            return List.of();
        }
    }
}
