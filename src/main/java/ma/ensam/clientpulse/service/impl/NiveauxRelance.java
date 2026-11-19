package ma.ensam.clientpulse.service.impl;


public final class NiveauxRelance {

    private static final int[] DECALAGES = {-3, 3, 15, 30, 45};

    private NiveauxRelance() {
    }

    public static int niveauAtteint(long joursApresEcheance) {
        int niveau = 0;
        for (int i = 0; i < DECALAGES.length; i++) {
            if (joursApresEcheance >= DECALAGES[i]) {
                niveau = i + 1;
            }
        }
        return niveau;
    }

    public static String libelle(int niveau) {
        int decalage = DECALAGES[niveau - 1];
        return decalage < 0 ? "J" + decalage : "J+" + decalage;
    }
}
