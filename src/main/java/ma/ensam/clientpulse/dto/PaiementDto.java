package ma.ensam.clientpulse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ma.ensam.clientpulse.domain.enums.ModePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un paiement. Sert en entrée (POST) et en sortie (GET).
 * En entrée, factureId vient de l'URL et id reste vide : le serveur les remplit.
 */
public record PaiementDto(Long id,
                          Long factureId,
                          @NotNull LocalDate datePaiement,
                          @NotNull @Positive BigDecimal montant,
                          @NotNull ModePaiement mode,
                          String reference) {
}
