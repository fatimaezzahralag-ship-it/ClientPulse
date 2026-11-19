package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FacturationService {

    /** statut peut être null : toutes les factures. */
    Page<FactureDto> lister(StatutFacture statut, Pageable pageable);

    FactureDto trouver(Long id);

    /** Crée une facture BROUILLON à partir d'un devis ACCEPTE (une facture au plus par devis). */
    FactureDto creerDepuisDevis(Long devisId);

    /**
     * Donne son numéro à la facture et la fige. dateEmission (défaut : aujourd'hui) et
     * dateEcheance (défaut : émission + 30 jours) sont facultatives.
     */
    FactureDto emettre(Long factureId, LocalDate dateEmission, LocalDate dateEcheance);

    /** Annule une facture émise qui n'a reçu aucun paiement. */
    FactureDto annuler(Long factureId);

    /** Passe en EN_RETARD les factures échues et impayées. Appelé chaque nuit. Renvoie leur nombre. */
    int marquerEnRetard(LocalDate jour);
}
