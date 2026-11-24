package ma.ensam.clientpulse.dto;

import java.time.LocalDateTime;

/** Une ligne de l'historique des imports. */
public record JournalImportDto(
        Long id,
        String fichier,
        int lignesLues,
        int lignesImportees,
        int lignesRejetees,
        LocalDateTime date,
        String creePar
) {
}
