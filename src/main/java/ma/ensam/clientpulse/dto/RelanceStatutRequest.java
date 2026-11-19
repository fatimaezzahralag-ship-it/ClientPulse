package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotNull;
import ma.ensam.clientpulse.domain.enums.StatutRelance;

/** Corps de PATCH /api/relances/{id}/statut : le nouveau statut et, si on veut, le résultat de l'action. */
public record RelanceStatutRequest(@NotNull StatutRelance statut, String resultat) {
}
