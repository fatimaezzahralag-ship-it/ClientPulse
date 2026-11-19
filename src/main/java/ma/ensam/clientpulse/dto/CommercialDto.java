package ma.ensam.clientpulse.dto;

/**
 * Commercial exposé pour les listes déroulantes et l'affichage de la fiche
 * client. Aucune donnée sensible (mot de passe) n'est exposée.
 */
public record CommercialDto(

        Long id,

        String nom,

        String prenom,

        String email,

        String equipe

) {}
