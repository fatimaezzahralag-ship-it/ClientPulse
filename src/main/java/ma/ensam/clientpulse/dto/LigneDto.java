package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Une ligne de devis ou de facture, vue depuis l'API.
 * Les deux entités (LigneDevis, LigneFacture) ont les mêmes champs, donc un seul DTO suffit.
 *
 * Si produitId est donné, désignation, prix et TVA peuvent être omis : ils sont repris du catalogue.
 * montantHT et montantTVA sont calculés par le serveur : le client les ignore à l'envoi.
 */
public record LigneDto(Long id,
                       Long produitId,
                       String designation,
                       @NotNull @Positive BigDecimal quantite,
                       @DecimalMin("0.00") BigDecimal prixUnitaireHT,
                       @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal remise,
                       @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal tauxTVA,
                       BigDecimal montantHT,
                       BigDecimal montantTVA) {
}
