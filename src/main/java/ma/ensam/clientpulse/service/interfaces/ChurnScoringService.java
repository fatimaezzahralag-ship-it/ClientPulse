package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.dto.ScoreClientDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChurnScoringService {

    /**
     * Calcule le score de risque d'un client à une date donnée et l'enregistre
     * dans l'historique.
     */
    ScoreClient calculer(Client client, LocalDate jour);

    /** Calcule le score d'un client existant à la date du jour. */
    ScoreClientDto calculerPourClient(Long clientId);

    /** Dernier score connu, vide si le client n'a jamais été évalué. */
    Optional<ScoreClientDto> dernierScore(Long clientId);

    /** Historique des scores, du plus récent au plus ancien. */
    List<ScoreClientDto> historique(Long clientId);
}
