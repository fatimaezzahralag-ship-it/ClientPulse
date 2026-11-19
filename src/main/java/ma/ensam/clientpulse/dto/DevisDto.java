package ma.ensam.clientpulse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ma.ensam.clientpulse.domain.enums.StatutDevis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Un devis vu depuis l'API. On renvoie clientId et non l'entité Client :
 * l'entité JPA ne sort jamais de la couche service (boucles JSON, chargement LAZY).
 *
 * À la création, on envoie : clientId, commercialId (facultatif), dateEmission (facultative, par défaut
 * aujourd'hui), dateValidite (facultative) et les lignes. Numéro, statut et totaux sont calculés par le serveur.
 */
public record DevisDto(Long id,
                       String numero,
                       LocalDate dateEmission,
                       LocalDate dateValidite,
                       StatutDevis statut,
                       @NotNull Long clientId,
                       Long commercialId,
                       @NotEmpty @Valid List<LigneDto> lignes,
                       BigDecimal totalHT,
                       BigDecimal totalTVA,
                       BigDecimal totalTTC) {
}
