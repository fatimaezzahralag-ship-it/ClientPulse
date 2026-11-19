package ma.ensam.clientpulse.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Le corps JSON de toute erreur renvoyée par l'API. Angular lit toujours la même forme.
 * details n'est rempli que pour les erreurs de validation : champ -> message.
 */
public record ErreurApi(LocalDateTime horodatage,
                        int statut,
                        String erreur,
                        String message,
                        Map<String, String> details) {
}
