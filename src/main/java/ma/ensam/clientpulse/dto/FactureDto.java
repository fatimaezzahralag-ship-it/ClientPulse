package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.domain.enums.TypeFacture;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Une facture (ou un avoir) vue depuis l'API.
 * totalPaye et resteAPayer ne sont pas en base : le mapper les lit sur l'entité Facture.
 * numero est null tant que la facture est en brouillon.
 */
public record FactureDto(Long id,
                         String numero,
                         TypeFacture type,
                         LocalDate dateEmission,
                         LocalDate dateEcheance,
                         StatutFacture statut,
                         Long clientId,
                         Long devisId,
                         Long factureOrigineId,
                         List<LigneDto> lignes,
                         List<PaiementDto> paiements,
                         BigDecimal totalHT,
                         BigDecimal totalTVA,
                         BigDecimal totalTTC,
                         BigDecimal totalPaye,
                         BigDecimal resteAPayer) {
}
