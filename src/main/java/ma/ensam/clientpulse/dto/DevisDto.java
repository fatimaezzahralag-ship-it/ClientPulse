package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.StatutDevis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Un devis vu depuis l'API. On renvoie clientId et non l'entité Client :
 * l'entité JPA ne sort jamais de la couche service (boucles JSON, chargement LAZY).
 */
public record DevisDto(Long id,
                       String numero,
                       LocalDate dateEmission,
                       LocalDate dateValidite,
                       StatutDevis statut,
                       Long clientId,
                       Long commercialId,
                       List<LigneDto> lignes,
                       BigDecimal totalHT,
                       BigDecimal totalTVA,
                       BigDecimal totalTTC) {
}
