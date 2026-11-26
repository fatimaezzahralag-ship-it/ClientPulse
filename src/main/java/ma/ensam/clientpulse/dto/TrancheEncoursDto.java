package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/** Montant des créances d'une tranche de retard et part dans l'encours total (en %). */
public record TrancheEncoursDto(String tranche, BigDecimal montant, BigDecimal pourcentage) {
}
