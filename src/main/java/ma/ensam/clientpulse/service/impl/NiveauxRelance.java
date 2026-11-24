package ma.ensam.clientpulse.service.impl;

/**
 * Les cinq niveaux de relance de paiement, repérés par rapport à l'échéance de la facture :
 * niveau 1 = J-3 (rappel avant l'échéance), 2 = J+3, 3 = J+15, 4 = J+30, 5 = J+45.
 * Classe utilitaire sans Spring : facile à tester.
 */
public final class NiveauxRelance {

    /** Décalage en jours par rapport à l'échéance, pour les niveaux 1 à 5. Négatif = avant l'échéance. */
    private static final int[] DECALAGES = {-3, 3, 15, 30, 45};

    private NiveauxRelance() {
    }

    /** Décalages par défaut (J-3, J+3, J+15, J+30, J+45), copie défensive. */
    public static int[] decalagesParDefaut() {
        return DECALAGES.clone();
    }

    /**
     * Le niveau le plus élevé atteint.
     *
     * @param joursApresEcheance jour de calcul - échéance (négatif si l'échéance n'est pas arrivée)
     * @return 0 si aucun niveau n'est atteint, sinon 1 à 5
     */
    public static int niveauAtteint(long joursApresEcheance) {
        return niveauAtteint(joursApresEcheance, DECALAGES);
    }

    /** Même calcul avec des décalages paramétrables (5 valeurs croissantes, négatif = avant l'échéance). */
    public static int niveauAtteint(long joursApresEcheance, int[] decalages) {
        int niveau = 0;
        for (int i = 0; i < decalages.length; i++) {
            if (joursApresEcheance >= decalages[i]) {
                niveau = i + 1;
            }
        }
        return niveau;
    }

    /** "J-3", "J+3", "J+15"... pour afficher le niveau dans le texte de la relance. */
    public static String libelle(int niveau) {
        return libelle(niveau, DECALAGES);
    }

    public static String libelle(int niveau, int[] decalages) {
        int decalage = decalages[niveau - 1];
        return decalage < 0 ? "J" + decalage : "J+" + decalage;
    }
}
