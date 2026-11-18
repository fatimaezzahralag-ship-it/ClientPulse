package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/**
 * Une ligne de devis ou de facture, vue depuis l'API.
 * Les deux entités (LigneDevis, LigneFacture) ont les mêmes champs, donc un seul DTO suffit.
 * montantHT et montantTVA sont calculés par le serveur : le client les ignore à l'envoi.
 */
public record LigneDto(Long id,
                       String designation,
                       BigDecimal quantite,
                       BigDecimal prixUnitaireHT,
                       BigDecimal remise,
                       BigDecimal tauxTVA,
                       BigDecimal montantHT,
                       BigDecimal montantTVA) {
}
