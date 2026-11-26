package ma.ensam.clientpulse.service.impl;

import com.opencsv.CSVWriter;

import ma.ensam.clientpulse.dao.interfaces.IndicateurDao;
import ma.ensam.clientpulse.dto.BalanceAgeeDto;
import ma.ensam.clientpulse.dto.ClientCaDto;
import ma.ensam.clientpulse.dto.DashboardDto;
import ma.ensam.clientpulse.dto.PointMensuelDto;
import ma.ensam.clientpulse.dto.SuccesRelancesDto;
import ma.ensam.clientpulse.dto.TrancheEncoursDto;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.service.interfaces.AnalyticsService;
import ma.ensam.clientpulse.service.interfaces.BalanceAgeeService;
import ma.ensam.clientpulse.service.interfaces.DashboardService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcul des indicateurs du tableau de bord. Aucune requête ici : tout passe par {@link IndicateurDao},
 * la balance âgée et l'analytique existantes.
 *
 * Définitions retenues (voir aussi la Javadoc de IndicateurDao) :
 * <ul>
 *   <li><b>Client actif</b> à une date : au moins une facture émise dans les 12 mois qui précèdent.</li>
 *   <li><b>Taux de rétention</b> = (clients fin − nouveaux clients) ÷ clients début × 100, borné à 100 %.</li>
 *   <li><b>Churn annuel</b> = 1 − rétention calculée sur 12 mois.</li>
 *   <li><b>CLV</b> = marge moyenne par facture × factures par client et par an × durée de vie
 *       (1 ÷ churn annuel, plafonnée à {@value #DUREE_VIE_MAX} ans quand le churn est nul).</li>
 *   <li><b>Encours</b> = somme des restes à payer (balance âgée) ; <b>DSO</b> = délai moyen entre émission
 *       et paiement complet ; <b>recouvrement</b> = encaissé ÷ facturé sur les factures échues de la période.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    static final int DUREE_VIE_MAX = 10;
    private static final BigDecimal CENT = BigDecimal.valueOf(100);
    private static final int NB_MEILLEURS_CLIENTS = 5;

    private final IndicateurDao indicateurDao;
    private final BalanceAgeeService balanceAgeeService;
    private final AnalyticsService analyticsService;

    public DashboardServiceImpl(IndicateurDao indicateurDao,
                                BalanceAgeeService balanceAgeeService,
                                AnalyticsService analyticsService) {
        this.indicateurDao = indicateurDao;
        this.balanceAgeeService = balanceAgeeService;
        this.analyticsService = analyticsService;
    }

    @Override
    public DashboardDto calculer(int periodeMois) {
        return calculer(periodeMois, LocalDate.now());
    }

    @Override
    public DashboardDto calculer(int periodeMois, LocalDate jour) {
        if (periodeMois < 1 || periodeMois > 12) {
            throw new RegleMetierException("La période doit être comprise entre 1 et 12 mois.");
        }
        YearMonth moisCourant = YearMonth.from(jour);
        YearMonth premierMois = moisCourant.minusMonths(periodeMois - 1L);
        LocalDate debutExclu = premierMois.atDay(1).minusDays(1);

        // ----- fidélisation
        Retention periode = retention(debutExclu, jour);
        Retention annuelle = periodeMois == 12
                ? periode
                : retention(YearMonth.from(jour).minusMonths(12).atEndOfMonth(), jour);
        BigDecimal churnAnnuel = BigDecimal.ONE
                .subtract(annuelle.taux().divide(CENT, 4, RoundingMode.HALF_UP))
                .max(BigDecimal.ZERO);

        // ----- valeur client (toujours sur 12 mois glissants, pour une CLV stable)
        LocalDate debut12 = YearMonth.from(jour).minusMonths(12).atEndOfMonth();
        BigDecimal marge = indicateurDao.margeMoyenneParFacture(debut12, jour);
        long factures12 = indicateurDao.compterFacturesEmises(debut12, jour);
        long actifs12 = indicateurDao.compterClientsActifs(jour);
        BigDecimal facturesParClient = actifs12 == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(factures12).divide(BigDecimal.valueOf(actifs12), 2, RoundingMode.HALF_UP);
        BigDecimal dureeVie = churnAnnuel.signum() == 0
                ? BigDecimal.valueOf(DUREE_VIE_MAX)
                : BigDecimal.ONE.divide(churnAnnuel, 2, RoundingMode.HALF_UP)
                        .min(BigDecimal.valueOf(DUREE_VIE_MAX));
        BigDecimal clv = marge.multiply(facturesParClient).multiply(dureeVie).setScale(2, RoundingMode.HALF_UP);

        // ----- trésorerie
        List<BalanceAgeeDto> balance = balanceAgeeService.calculer(jour);
        BigDecimal nonEchu = somme(balance, BalanceAgeeDto::nonEchu);
        BigDecimal r1a30 = somme(balance, BalanceAgeeDto::retard1a30);
        BigDecimal r31a60 = somme(balance, BalanceAgeeDto::retard31a60);
        BigDecimal r61a90 = somme(balance, BalanceAgeeDto::retard61a90);
        BigDecimal r90 = somme(balance, BalanceAgeeDto::retardPlusDe90);
        BigDecimal encours = nonEchu.add(r1a30).add(r31a60).add(r61a90).add(r90);
        BigDecimal encoursEchu = encours.subtract(nonEchu);

        List<TrancheEncoursDto> tranches = List.of(
                tranche("Non échu", nonEchu, encours),
                tranche("1 à 30 j", r1a30, encours),
                tranche("31 à 60 j", r31a60, encours),
                tranche("61 à 90 j", r61a90, encours),
                tranche("Plus de 90 j", r90, encours));

        BigDecimal echu = indicateurDao.montantFactureEcheance(debutExclu, jour);
        BigDecimal encaisseSurEchu = indicateurDao.encaisseSurFacturesEcheance(debutExclu, jour);

        // ----- relances
        SuccesRelancesDto relances = indicateurDao.succesRelances(debutExclu, jour);

        return new DashboardDto(
                jour,
                periodeMois,
                periode.debut(),
                periode.fin(),
                periode.nouveaux(),
                periode.taux(),
                churnAnnuel.multiply(CENT).setScale(2, RoundingMode.HALF_UP),
                echelle(marge),
                facturesParClient,
                dureeVie,
                clv,
                echelle(indicateurDao.chiffreAffairesHt(debutExclu, jour)),
                echelle(indicateurDao.totalFactureTtc(debutExclu, jour)),
                echelle(indicateurDao.totalEncaisse(debutExclu, jour)),
                echelle(encours),
                echelle(encoursEchu),
                indicateurDao.delaiMoyenPaiement(debutExclu, jour).setScale(1, RoundingMode.HALF_UP),
                pourcentage(encaisseSurEchu, echu),
                relances.effectuees(),
                relances.reussies(),
                pourcentage(BigDecimal.valueOf(relances.reussies()), BigDecimal.valueOf(relances.effectuees())),
                serieMensuelle(premierMois, moisCourant, debutExclu, jour),
                tranches,
                meilleursClients(debutExclu, jour),
                analyticsService.repartitionSegments(),
                analyticsService.repartitionNiveaux());
    }

    @Override
    public String exporterCsv(int periodeMois, LocalDate jour) {
        DashboardDto d = calculer(periodeMois, jour);
        StringWriter sortie = new StringWriter();
        sortie.write('\uFEFF');   // BOM : Excel reconnaît l'UTF-8
        try (CSVWriter csv = new CSVWriter(sortie, ';', '"', '"', "\n")) {
            csv.writeNext(new String[]{"Indicateur", "Valeur", "Unité"});
            ligne(csv, "Date de référence", d.dateReference().toString(), "");
            ligne(csv, "Période", String.valueOf(d.periodeMois()), "mois");
            ligne(csv, "Clients en début de période", String.valueOf(d.clientsDebutPeriode()), "clients");
            ligne(csv, "Clients en fin de période", String.valueOf(d.clientsFinPeriode()), "clients");
            ligne(csv, "Nouveaux clients", String.valueOf(d.nouveauxClients()), "clients");
            ligne(csv, "Taux de rétention", d.tauxRetention().toPlainString(), "%");
            ligne(csv, "Churn annuel", d.tauxChurnAnnuel().toPlainString(), "%");
            ligne(csv, "Marge moyenne par facture", d.margeMoyenneParFacture().toPlainString(), "MAD");
            ligne(csv, "Factures par client et par an", d.facturesParClientParAn().toPlainString(), "");
            ligne(csv, "Durée de vie estimée", d.dureeVieAnnees().toPlainString(), "ans");
            ligne(csv, "CLV", d.clv().toPlainString(), "MAD");
            ligne(csv, "Chiffre d'affaires HT", d.chiffreAffairesHt().toPlainString(), "MAD");
            ligne(csv, "Facturé TTC", d.totalFactureTtc().toPlainString(), "MAD");
            ligne(csv, "Encaissé", d.totalEncaisse().toPlainString(), "MAD");
            ligne(csv, "Encours clients", d.encours().toPlainString(), "MAD");
            ligne(csv, "Dont échu", d.encoursEchu().toPlainString(), "MAD");
            ligne(csv, "DSO", d.dso().toPlainString(), "jours");
            ligne(csv, "Taux de recouvrement", d.tauxRecouvrement().toPlainString(), "%");
            ligne(csv, "Relances effectuées", String.valueOf(d.relancesEffectuees()), "");
            ligne(csv, "Taux de succès des relances", d.tauxSuccesRelances().toPlainString(), "%");
            csv.writeNext(new String[]{});
            csv.writeNext(new String[]{"Mois", "Facturé TTC (MAD)", "Encaissé (MAD)"});
            for (PointMensuelDto point : d.serieMensuelle()) {
                csv.writeNext(new String[]{point.mois(), point.facture().toPlainString(), point.encaisse().toPlainString()});
            }
            csv.writeNext(new String[]{});
            csv.writeNext(new String[]{"Tranche de retard", "Montant (MAD)", "Part de l'encours (%)"});
            for (TrancheEncoursDto t : d.encoursParTranche()) {
                csv.writeNext(new String[]{t.tranche(), t.montant().toPlainString(), t.pourcentage().toPlainString()});
            }
        } catch (IOException e) {
            throw new IllegalStateException("Export CSV impossible", e);
        }
        return sortie.toString();
    }

    // ------------------------------------------------------------------ calculs intermédiaires

    private record Retention(long debut, long fin, long nouveaux, BigDecimal taux) {
    }

    /** Rétention entre {@code debutExclu} et {@code jour} : (fin − nouveaux) ÷ début × 100, bornée à [0 ; 100]. */
    private Retention retention(LocalDate debutExclu, LocalDate jour) {
        long debut = indicateurDao.compterClientsActifs(debutExclu);
        long fin = indicateurDao.compterClientsActifs(jour);
        long nouveaux = indicateurDao.compterNouveauxClients(debutExclu, jour);
        BigDecimal taux = BigDecimal.ZERO;
        if (debut > 0) {
            taux = BigDecimal.valueOf(fin - nouveaux)
                    .multiply(CENT)
                    .divide(BigDecimal.valueOf(debut), 2, RoundingMode.HALF_UP)
                    .max(BigDecimal.ZERO)
                    .min(CENT);
        }
        return new Retention(debut, fin, nouveaux, taux);
    }

    /** Un point par mois civil de la période, y compris les mois sans activité (valeurs à zéro). */
    private List<PointMensuelDto> serieMensuelle(YearMonth premier, YearMonth dernier,
                                                 LocalDate debutExclu, LocalDate jour) {
        Map<String, BigDecimal> factures = new HashMap<>();
        for (PointMensuelDto p : indicateurDao.facturationMensuelle(debutExclu, jour)) {
            factures.put(p.mois(), p.facture());
        }
        Map<String, BigDecimal> encaissements = new HashMap<>();
        for (PointMensuelDto p : indicateurDao.encaissementMensuel(debutExclu, jour)) {
            encaissements.put(p.mois(), p.encaisse());
        }
        List<PointMensuelDto> serie = new ArrayList<>();
        for (YearMonth mois = premier; !mois.isAfter(dernier); mois = mois.plusMonths(1)) {
            String cle = mois.toString();   // AAAA-MM, même format que DATE_FORMAT('%Y-%m')
            serie.add(new PointMensuelDto(
                    cle,
                    echelle(factures.getOrDefault(cle, BigDecimal.ZERO)),
                    echelle(encaissements.getOrDefault(cle, BigDecimal.ZERO))));
        }
        return serie;
    }

    private List<ClientCaDto> meilleursClients(LocalDate debutExclu, LocalDate jour) {
        return indicateurDao.meilleursClients(debutExclu, jour, NB_MEILLEURS_CLIENTS).stream()
                .map(c -> new ClientCaDto(c.clientId(), c.raisonSociale(), echelle(c.chiffreAffairesHt())))
                .toList();
    }

    private static TrancheEncoursDto tranche(String libelle, BigDecimal montant, BigDecimal total) {
        return new TrancheEncoursDto(libelle, echelle(montant), pourcentage(montant, total));
    }

    private static BigDecimal pourcentage(BigDecimal partie, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return partie.multiply(CENT).divide(total, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal somme(List<BalanceAgeeDto> lignes,
                                    java.util.function.Function<BalanceAgeeDto, BigDecimal> champ) {
        BigDecimal total = BigDecimal.ZERO;
        for (BalanceAgeeDto ligne : lignes) {
            total = total.add(champ.apply(ligne));
        }
        return total;
    }

    private static BigDecimal echelle(BigDecimal montant) {
        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    private static void ligne(CSVWriter csv, String libelle, String valeur, String unite) {
        csv.writeNext(new String[]{libelle, valeur, unite});
    }
}
