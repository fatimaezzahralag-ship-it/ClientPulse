package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Indicateurs clés du tableau de bord (règle 7.10 du cahier des charges).
 *
 * Les montants sont en dirhams, arrondis à 2 décimales ; les taux sont en pourcentage.
 * La période est alignée sur les mois civils : {@code periodeMois = 12} au 19/11/2026
 * couvre de décembre 2025 au 19/11/2026.
 */
public record DashboardDto(
        LocalDate dateReference,
        int periodeMois,

        // ----- fidélisation
        long clientsDebutPeriode,
        long clientsFinPeriode,
        long nouveauxClients,
        BigDecimal tauxRetention,
        BigDecimal tauxChurnAnnuel,

        // ----- valeur client
        BigDecimal margeMoyenneParFacture,
        BigDecimal facturesParClientParAn,
        BigDecimal dureeVieAnnees,
        BigDecimal clv,

        // ----- trésorerie
        BigDecimal chiffreAffairesHt,
        BigDecimal totalFactureTtc,
        BigDecimal totalEncaisse,
        BigDecimal encours,
        BigDecimal encoursEchu,
        BigDecimal dso,
        BigDecimal tauxRecouvrement,

        // ----- relances
        long relancesEffectuees,
        long relancesReussies,
        BigDecimal tauxSuccesRelances,

        // ----- graphiques
        List<PointMensuelDto> serieMensuelle,
        List<TrancheEncoursDto> encoursParTranche,
        List<ClientCaDto> meilleursClients,
        Map<String, Long> repartitionSegments,
        Map<String, Long> repartitionNiveaux
) {
}
