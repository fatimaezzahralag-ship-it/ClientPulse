package ma.ensam.clientpulse.service;

import java.math.BigDecimal;

/**
 * Résultat d'un facteur de risque.
 *
 * @param code        identifiant stable du facteur (INACTIVITE, ...)
 * @param note        risque apporté par le facteur, entre 0 et 100
 * @param explication phrase compréhensible par l'utilisateur final
 */
public record ResultatFacteur(
        String code,
        BigDecimal note,
        String explication
) {}
