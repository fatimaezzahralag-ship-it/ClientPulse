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
        BigDecimal montantAchats3MoisPrecedents,

        /** Date du dernier achat (facture emise non annulee), null si aucun. */
        LocalDate dernierAchat,

        /** Date du premier achat, null si aucun. */
        LocalDate premierAchat,

        /** Nombre de factures emises sur les 12 derniers mois (frequence RFM). */
        Long nombreAchats12Mois,

        /** Montant TTC des factures des 12 derniers mois (monetaire RFM). */
        BigDecimal montantAchats12Mois

) {

    /**
     * Constructeur de compatibilité pour les appels qui ne concernent que le
     * churn (J3) : les indicateurs RFM n'étaient pas encore exposés.
     */
    public StatistiqueClientDto(
            Long clientId,
            LocalDate derniereActivite,
            BigDecimal montantFacture,
            Long reclamationsOuvertes,
            BigDecimal retardMoyenPaiement,
            BigDecimal montantAchats3DerniersMois,
            BigDecimal montantAchats3MoisPrecedents) {
        this(
                clientId,
                derniereActivite,
                montantFacture,
                reclamationsOuvertes,
                retardMoyenPaiement,
                montantAchats3DerniersMois,
                montantAchats3MoisPrecedents,
                null,
                null,
                0L,
                BigDecimal.ZERO
        );
    }
}
