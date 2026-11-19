package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotNull;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Une relance. P2 l'utilise pour créer ses relances commerciales : nature, niveau, clientId et contenu
 * suffisent. dateCreation et statut (A_FAIRE) sont remplis par le serveur.
 */
public record RelanceDto(Long id,
                         @NotNull NatureRelance nature,
                         int niveau,
                         LocalDateTime dateCreation,
                         LocalDate echeance,
                         StatutRelance statut,
                         String resultat,
                         String contenu,
                         boolean suggereeParSysteme,
                         @NotNull Long clientId,
                         Long factureId,
                         Long affecteAId) {
}
