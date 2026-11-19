package ma.ensam.clientpulse.domain.enums;

/**
 * Tranches de la balance âgée : on classe une créance selon son nombre de jours de retard.
 * Exemple : une facture échue depuis 20 jours est dans DE_1_A_30.
 */
public enum TrancheRetard {
    NON_ECHU, DE_1_A_30, DE_31_A_60, DE_61_A_90, PLUS_DE_90;

    /**
     * @param joursDeRetard échéance -> jour de calcul. Zéro ou négatif : pas encore échue.
     */
    public static TrancheRetard pour(long joursDeRetard) {
        if (joursDeRetard <= 0) {
            return NON_ECHU;
        }
        if (joursDeRetard <= 30) {
            return DE_1_A_30;
        }
        if (joursDeRetard <= 60) {
            return DE_31_A_60;
        }
        if (joursDeRetard <= 90) {
            return DE_61_A_90;
        }
        return PLUS_DE_90;
    }
}
