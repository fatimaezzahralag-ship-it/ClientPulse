package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;

import java.time.LocalDate;

/**
 * Segmentation RFM (Recency, Frequency, Monetary).
 *
 * Source unique de vérité : le churn appelle {@link #segmenter(Client, LocalDate)}
 * pour renseigner le segment du score qu'il enregistre, et le job nocturne
 * appelle {@link #segmenter(LocalDate)} pour rafraîchir les segments déjà
 * stockés sans avoir à recalculer tous les scores.
 */
public interface RfmService {

    /** Calcule le segment d'un client à partir des statistiques d'achat. */
    SegmentRFM segmenter(Client client, LocalDate jour);

    /**
     * Recalcule, par lots, le segment des derniers scores enregistrés.
     * Le traitement est idempotent et tolère les clients invalides.
     */
    void segmenter(LocalDate jour);
}
