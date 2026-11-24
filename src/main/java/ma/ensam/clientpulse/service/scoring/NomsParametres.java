package ma.ensam.clientpulse.service.scoring;

/**
 * Noms des paramètres stockés dans la table {@code parametres_scoring}.
 *
 * Centraliser les noms évite les fautes de frappe silencieuses : un paramètre
 * absent est signalé par une exception explicite au moment du calcul.
 */
public final class NomsParametres {

    // Poids des facteurs : la somme doit valoir 1.
    public static final String POIDS_INACTIVITE = "POIDS_INACTIVITE";
    public static final String POIDS_BAISSE_ACHATS = "POIDS_BAISSE_ACHATS";
    public static final String POIDS_RECLAMATIONS = "POIDS_RECLAMATIONS";
    public static final String POIDS_RETARD_PAIEMENT = "POIDS_RETARD_PAIEMENT";

    // Seuils de niveau de risque.
    public static final String SEUIL_RISQUE_MOYEN = "SEUIL_RISQUE_MOYEN";
    public static final String SEUIL_RISQUE_ELEVE = "SEUIL_RISQUE_ELEVE";
    public static final String SEUIL_RISQUE_CRITIQUE = "SEUIL_RISQUE_CRITIQUE";

    // Seuils du facteur inactivité (en jours).
    public static final String SEUIL_INACTIVITE_FAIBLE =
            "SEUIL_INACTIVITE_FAIBLE";
    public static final String SEUIL_INACTIVITE_MOYEN =
            "SEUIL_INACTIVITE_MOYEN";
    public static final String SEUIL_INACTIVITE_ELEVE =
            "SEUIL_INACTIVITE_ELEVE";

    // Seuils du facteur baisse des achats (en pourcentage).
    public static final String SEUIL_BAISSE_FAIBLE = "SEUIL_BAISSE_FAIBLE";
    public static final String SEUIL_BAISSE_MOYEN = "SEUIL_BAISSE_MOYEN";
    public static final String SEUIL_BAISSE_ELEVE = "SEUIL_BAISSE_ELEVE";

    // Seuils du facteur réclamations (nombre de réclamations ouvertes).
    public static final String SEUIL_RECLAMATIONS_MOYEN =
            "SEUIL_RECLAMATIONS_MOYEN";
    public static final String SEUIL_RECLAMATIONS_ELEVE =
            "SEUIL_RECLAMATIONS_ELEVE";

    // Seuils du facteur retard de paiement (en jours).
    public static final String SEUIL_RETARD_FAIBLE = "SEUIL_RETARD_FAIBLE";
    public static final String SEUIL_RETARD_MOYEN = "SEUIL_RETARD_MOYEN";

    // Seuils RFM (J4). R et les durées sont en jours, F en nombre d'achats
    // sur 12 mois, M en dirhams. Voir la migration V5 pour les valeurs par
    // défaut et l'ordre d'évaluation des segments.
    public static final String RFM_RECENCE_CHAMPION = "RFM_RECENCE_CHAMPION";
    public static final String RFM_RECENCE_LOYAL = "RFM_RECENCE_LOYAL";
    public static final String RFM_FREQUENCE_CHAMPION = "RFM_FREQUENCE_CHAMPION";
    public static final String RFM_FREQUENCE_LOYAL = "RFM_FREQUENCE_LOYAL";
    public static final String RFM_MONETAIRE_CHAMPION = "RFM_MONETAIRE_CHAMPION";
    public static final String RFM_MONETAIRE_LOYAL = "RFM_MONETAIRE_LOYAL";
    public static final String RFM_NOUVEAU_JOURS = "RFM_NOUVEAU_JOURS";
    public static final String RFM_HIBERNATION_JOURS =
            "RFM_HIBERNATION_JOURS";
    public static final String RFM_PERDU_JOURS = "RFM_PERDU_JOURS";

    // Délais des relances de paiement (V8). N1 = jours AVANT l'échéance (rappel
    // préventif), N2 à N5 = jours APRÈS l'échéance. Toujours positifs en base.
    public static final String DELAI_RELANCE_N1 = "DELAI_RELANCE_N1";
    public static final String DELAI_RELANCE_N2 = "DELAI_RELANCE_N2";
    public static final String DELAI_RELANCE_N3 = "DELAI_RELANCE_N3";
    public static final String DELAI_RELANCE_N4 = "DELAI_RELANCE_N4";
    public static final String DELAI_RELANCE_N5 = "DELAI_RELANCE_N5";

    private NomsParametres() {
    }
}
