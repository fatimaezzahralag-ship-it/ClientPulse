package ma.ensam.clientpulse.domain.enums;

public enum TrancheRetard {
    NON_ECHU, DE_1_A_30, DE_31_A_60, DE_61_A_90, PLUS_DE_90;

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
