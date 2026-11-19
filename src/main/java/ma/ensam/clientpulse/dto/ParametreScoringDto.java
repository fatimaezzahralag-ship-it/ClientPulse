package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/** Paramètre du moteur exposé à l'écran d'administration. */
public record ParametreScoringDto(

        Long id,

        String nom,

        BigDecimal valeur

) {}
