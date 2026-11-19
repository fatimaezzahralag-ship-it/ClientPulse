package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotNull;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;

/** Résultat saisi par le commercial lors du traitement d'une relance. */
public record ResultatRelanceRequest(

        @NotNull ResultatRelance resultat,

        String commentaire

) {}
