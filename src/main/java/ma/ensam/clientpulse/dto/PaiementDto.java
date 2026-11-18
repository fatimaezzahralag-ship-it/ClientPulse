package ma.ensam.clientpulse.dto;

import ma.ensam.clientpulse.domain.enums.ModePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un paiement. Sert en entrée (POST) et en sortie (GET).
 * En entrée, factureId vient de l'URL et id reste vide : le serveur les remplit.
 */
public record PaiementDto(Long id,
                          Long factureId,
                          LocalDate datePaiement,
                          BigDecimal montant,
                          ModePaiement mode,
                          String reference) {
}
