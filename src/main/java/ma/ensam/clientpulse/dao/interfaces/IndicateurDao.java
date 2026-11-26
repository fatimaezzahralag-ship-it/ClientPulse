package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.dto.ClientCaDto;
import ma.ensam.clientpulse.dto.PointMensuelDto;
import ma.ensam.clientpulse.dto.SuccesRelancesDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lectures agrégées pour le tableau de bord (rétention, CLV, trésorerie, relances).
 *
 * Seul point d'accès aux agrégats financiers : DashboardService ne dépend ni de FactureDao ni de
 * PaiementDao. Toutes les périodes sont exprimées par une borne de début EXCLUE et une borne de fin
 * INCLUSE ({@code ]debutExclu ; finInclus]}). Les avoirs et les factures annulées sont ignorés.
 */
public interface IndicateurDao {

    /** Clients ayant au moins une facture émise dans les 12 mois qui précèdent {@code date}. */
    long compterClientsActifs(LocalDate date);

    /** Clients dont la toute première facture émise tombe dans la période. */
    long compterNouveauxClients(LocalDate debutExclu, LocalDate finInclus);

    long compterFacturesEmises(LocalDate debutExclu, LocalDate finInclus);

    /**
     * Marge moyenne par facture, en dirhams : somme des (montant HT de ligne × marge du produit)
     * des factures qui ont au moins une ligne liée à un produit renseigné avec une marge.
     * Retourne zéro s'il n'y en a aucune.
     */
    BigDecimal margeMoyenneParFacture(LocalDate debutExclu, LocalDate finInclus);

    BigDecimal chiffreAffairesHt(LocalDate debutExclu, LocalDate finInclus);

    BigDecimal totalFactureTtc(LocalDate debutExclu, LocalDate finInclus);

    BigDecimal totalEncaisse(LocalDate debutExclu, LocalDate finInclus);

    /** Moyenne des jours entre l'émission et le dernier paiement des factures soldées dans la période (0 si aucune). */
    BigDecimal delaiMoyenPaiement(LocalDate debutExclu, LocalDate finInclus);

    /** Total TTC des factures dont l'échéance tombe dans la période. */
    BigDecimal montantFactureEcheance(LocalDate debutExclu, LocalDate finInclus);

    /** Paiements déjà reçus (jusqu'à {@code finInclus}) sur ces mêmes factures. */
    BigDecimal encaisseSurFacturesEcheance(LocalDate debutExclu, LocalDate finInclus);

    /**
     * Relances terminées créées dans la période, et celles suivies d'un paiement de la facture
     * (relance de paiement) ou d'une nouvelle facture du client (relance commerciale) dans les 30 jours.
     */
    SuccesRelancesDto succesRelances(LocalDate debutExclu, LocalDate finInclus);

    /** Total TTC facturé par mois ({@code encaisse} à zéro), trié par mois ; les mois sans facture sont absents. */
    List<PointMensuelDto> facturationMensuelle(LocalDate debutExclu, LocalDate finInclus);

    /** Total encaissé par mois ({@code facture} à zéro), trié par mois ; les mois sans paiement sont absents. */
    List<PointMensuelDto> encaissementMensuel(LocalDate debutExclu, LocalDate finInclus);

    List<ClientCaDto> meilleursClients(LocalDate debutExclu, LocalDate finInclus, int limite);
}
