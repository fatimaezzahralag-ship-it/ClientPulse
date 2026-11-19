package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Un produit du catalogue. À l'envoi on fournit tauxTvaId ;
 * à la réponse, le serveur ajoute le taux lui-même (tauxTva) pour éviter un second appel.
 */
public record ProduitDto(Long id,
                         @NotBlank String reference,
                         @NotBlank String designation,
                         @NotNull @DecimalMin("0.00") BigDecimal prixUnitaireHT,
                         @DecimalMin("0.00") BigDecimal marge,
                         boolean actif,
                         @NotNull Long tauxTvaId,
                         BigDecimal tauxTva) {
}
