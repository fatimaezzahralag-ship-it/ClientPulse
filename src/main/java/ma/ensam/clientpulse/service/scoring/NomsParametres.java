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

    private NomsParametres() {
    }
}
