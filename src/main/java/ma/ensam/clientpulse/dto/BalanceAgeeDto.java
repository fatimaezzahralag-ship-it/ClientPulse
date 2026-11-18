package ma.ensam.clientpulse.dto;

import java.math.BigDecimal;

/**
 * Balance âgée d'un client : ce qu'il nous doit, classé selon le retard de paiement.
 * Chaque champ est un montant TTC restant à payer.
 * Elle sera remplie par un service en J3 ; ici on définit seulement la forme des données.
 */
public record BalanceAgeeDto(Long clientId,
                             BigDecimal nonEchu,
                             BigDecimal retard1a30,
                             BigDecimal retard31a60,
                             BigDecimal retard61a90,
                             BigDecimal retardPlusDe90) {

    /** Total dû, toutes tranches confondues. */
    public BigDecimal total() {
        return nonEchu.add(retard1a30).add(retard31a60).add(retard61a90).add(retardPlusDe90);
    }
}
