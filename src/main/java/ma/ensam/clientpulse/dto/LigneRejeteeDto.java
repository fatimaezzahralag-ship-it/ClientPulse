package ma.ensam.clientpulse.dto;

/**
 * Une ligne du fichier CSV refusée à l'import.
 *
 * @param numeroLigne numéro de la ligne dans le fichier (l'en-tête est la ligne 1)
 * @param code        code client lu sur la ligne (peut être vide)
 * @param motif       raison du rejet, lisible par l'utilisateur
 */
public record LigneRejeteeDto(int numeroLigne, String code, String motif) {
}
