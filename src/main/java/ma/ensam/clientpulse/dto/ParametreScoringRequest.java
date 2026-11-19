package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Modification d'un paramètre. Le nom est facultatif mais vérifié si fourni. */
public record ParametreScoringRequest(

        String nom,

        @NotNull
        @DecimalMin(value = "0", message = "La valeur doit être positive")
        BigDecimal valeur

) {}
