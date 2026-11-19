package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection agrégée des données nécessaires au moteur de scoring.
 *
 * Toutes les valeurs sont calculées par {@code StatistiqueClientDao} : le
 * service de scoring n'interroge jamais les factures ou les paiements
 * directement.
 */
public record StatistiqueClientDto(

        Long clientId,

        /** Date de la dernière interaction enregistrée, {@code null} si aucune. */
        LocalDate derniereActivite,

        /** Montant TTC cumulé des factures non annulées. */
        BigDecimal montantFacture,

        /** Nombre de réclamations encore ouvertes ou en cours. */
        Long reclamationsOuvertes,

        /** Retard moyen de paiement en jours (0 si tout est payé à temps). */
        BigDecimal retardMoyenPaiement,

        /** Montant TTC facturé sur les 3 derniers mois. */
        BigDecimal montantAchats3DerniersMois,

        /** Montant TTC facturé sur les 3 mois précédant les 3 derniers. */
        BigDecimal montantAchats3MoisPrecedents

) {}
