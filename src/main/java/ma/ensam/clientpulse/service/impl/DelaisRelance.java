package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ParametreScoringDao;
import ma.ensam.clientpulse.service.scoring.NomsParametres;

import org.springframework.stereotype.Component;

/**
 * Délais paramétrables des relances de paiement (règle 7.5 du cahier des charges).
 *
 * Les valeurs sont lues dans {@code parametres_scoring} (migration V8) : l'administrateur
 * les modifie depuis l'écran des paramètres, et elles sont prises en compte au prochain
 * passage du traitement de nuit. Si un paramètre est absent, la valeur par défaut est
 * utilisée (J-3, J+3, J+15, J+30, J+45).
 *
 * En base, tous les délais sont positifs : le niveau 1 est un nombre de jours AVANT
 * l'échéance, il est converti ici en décalage négatif.
 */
@Component
public class DelaisRelance {

    private final ParametreScoringDao parametreScoringDao;

    public DelaisRelance(ParametreScoringDao parametreScoringDao) {
        this.parametreScoringDao = parametreScoringDao;
    }

    /** Version sans base de données : toujours les valeurs par défaut (utile aux tests). */
    public static DelaisRelance parDefaut() {
        return new DelaisRelance(null);
    }

    /** Cinq décalages en jours par rapport à l'échéance (négatif = avant l'échéance). */
    public int[] decalages() {
        int[] decalages = NiveauxRelance.decalagesParDefaut();
        if (parametreScoringDao == null) {
            return decalages;
        }
        decalages[0] = -lire(NomsParametres.DELAI_RELANCE_N1, -decalages[0]);
        decalages[1] = lire(NomsParametres.DELAI_RELANCE_N2, decalages[1]);
        decalages[2] = lire(NomsParametres.DELAI_RELANCE_N3, decalages[2]);
        decalages[3] = lire(NomsParametres.DELAI_RELANCE_N4, decalages[3]);
        decalages[4] = lire(NomsParametres.DELAI_RELANCE_N5, decalages[4]);
        return decalages;
    }

    private int lire(String nom, int valeurParDefaut) {
        return parametreScoringDao.findByNom(nom)
                .map(parametre -> parametre.getValeur().intValue())
                .orElse(valeurParDefaut);
    }
}
