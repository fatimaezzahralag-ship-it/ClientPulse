package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.ResultatTraitement;

import java.time.LocalDate;

public interface RelancePaiementService {

    /**
     * Crée les relances de paiement du jour : une seule fois par niveau (J-3, J+3, J+15, J+30, J+45)
     * et par facture impayée. Renvoie le nombre de relances créées.
     */
    int genererRelances(LocalDate jour);

    /** Traitement complet de la nuit : factures en retard, puis relances. */
    ResultatTraitement traiterJour(LocalDate jour);
}
