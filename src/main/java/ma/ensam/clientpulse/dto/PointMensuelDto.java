package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/** Un mois de la courbe « facturé / encaissé » du tableau de bord ({@code mois} au format AAAA-MM). */
public record PointMensuelDto(String mois, BigDecimal facture, BigDecimal encaisse) {
}
