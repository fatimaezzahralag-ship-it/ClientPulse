package ma.ensam.clientpulse.dto;

import java.util.List;

/**
 * Compte rendu d'un import CSV.
 *
 * @param journalId       identifiant de la ligne créée dans le journal d'import
 * @param fichier         nom du fichier importé
 * @param lignesLues      lignes de données lues (lignes vides ignorées)
 * @param lignesImportees clients créés
 * @param lignesRejetees  lignes refusées
 * @param rejets          détail des rejets (limité aux 200 premiers)
 */
public record RapportImportDto(
        Long journalId,
        String fichier,
        int lignesLues,
        int lignesImportees,
        int lignesRejetees,
        List<LigneRejeteeDto> rejets
) {
}
