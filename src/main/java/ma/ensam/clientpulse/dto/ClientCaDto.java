package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/** Un client et son chiffre d'affaires HT sur la période (classement du tableau de bord). */
public record ClientCaDto(Long clientId, String raisonSociale, BigDecimal chiffreAffairesHt) {
}
