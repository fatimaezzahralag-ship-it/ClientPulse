package ma.ensam.clientpulse.domain.entity;

import java.math.BigDecimal;

/**
 * Ce qu'il faut savoir d'une ligne pour calculer ses montants.
 * LigneDevis et LigneFacture l'implémentent : le calculateur n'a donc pas besoin de les connaître.
 * remise et tauxTVA sont en pourcentage (10 = 10 %).
 */
public interface LigneCalculable {

    BigDecimal getQuantite();

    BigDecimal getPrixUnitaireHT();

    BigDecimal getRemise();

    BigDecimal getTauxTVA();
}
