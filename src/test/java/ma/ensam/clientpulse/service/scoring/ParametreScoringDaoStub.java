package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.ParametreScoringDao;
import ma.ensam.clientpulse.domain.entity.ParametreScoring;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Doublure en mémoire du DAO des paramètres pour les tests unitaires.
 * Reprend exactement les valeurs de la migration V4.
 */
public class ParametreScoringDaoStub implements ParametreScoringDao {

    private final Map<String, BigDecimal> valeurs = new LinkedHashMap<>();

    public ParametreScoringDaoStub() {
        valeurs.put(NomsParametres.POIDS_INACTIVITE, valeur("0.30"));
        valeurs.put(NomsParametres.POIDS_BAISSE_ACHATS, valeur("0.25"));
        valeurs.put(NomsParametres.POIDS_RECLAMATIONS, valeur("0.15"));
        valeurs.put(NomsParametres.POIDS_RETARD_PAIEMENT, valeur("0.30"));

        valeurs.put(NomsParametres.SEUIL_RISQUE_MOYEN, valeur("40"));
        valeurs.put(NomsParametres.SEUIL_RISQUE_ELEVE, valeur("70"));
        valeurs.put(NomsParametres.SEUIL_RISQUE_CRITIQUE, valeur("90"));

        valeurs.put(NomsParametres.SEUIL_INACTIVITE_FAIBLE, valeur("30"));
        valeurs.put(NomsParametres.SEUIL_INACTIVITE_MOYEN, valeur("90"));
        valeurs.put(NomsParametres.SEUIL_INACTIVITE_ELEVE, valeur("180"));

        valeurs.put(NomsParametres.SEUIL_BAISSE_FAIBLE, valeur("20"));
        valeurs.put(NomsParametres.SEUIL_BAISSE_MOYEN, valeur("50"));
        valeurs.put(NomsParametres.SEUIL_BAISSE_ELEVE, valeur("75"));

        valeurs.put(NomsParametres.SEUIL_RECLAMATIONS_MOYEN, valeur("1"));
        valeurs.put(NomsParametres.SEUIL_RECLAMATIONS_ELEVE, valeur("2"));

        valeurs.put(NomsParametres.SEUIL_RETARD_FAIBLE, valeur("7"));
        valeurs.put(NomsParametres.SEUIL_RETARD_MOYEN, valeur("30"));

        // Seuils RFM de la migration V5.
        valeurs.put(NomsParametres.RFM_RECENCE_CHAMPION, valeur("30"));
        valeurs.put(NomsParametres.RFM_RECENCE_LOYAL, valeur("90"));
        valeurs.put(NomsParametres.RFM_FREQUENCE_CHAMPION, valeur("10"));
        valeurs.put(NomsParametres.RFM_FREQUENCE_LOYAL, valeur("4"));
        valeurs.put(NomsParametres.RFM_MONETAIRE_CHAMPION, valeur("50000"));
        valeurs.put(NomsParametres.RFM_MONETAIRE_LOYAL, valeur("10000"));
        valeurs.put(NomsParametres.RFM_NOUVEAU_JOURS, valeur("90"));
        valeurs.put(NomsParametres.RFM_HIBERNATION_JOURS, valeur("240"));
        valeurs.put(NomsParametres.RFM_PERDU_JOURS, valeur("365"));
    }

    public ParametreScoringDaoStub avec(String nom, String nouvelleValeur) {
        valeurs.put(nom, valeur(nouvelleValeur));
        return this;
    }

    public ParametreScoringDaoStub sans(String nom) {
        valeurs.remove(nom);
        return this;
    }

    @Override
    public Optional<ParametreScoring> findByNom(String nom) {
        BigDecimal v = valeurs.get(nom);
        return v == null
                ? Optional.empty()
                : Optional.of(new ParametreScoring(nom, v));
    }

    @Override
    public Optional<ParametreScoring> findById(Long id) {
        // Les tests unitaires utilisent le plus souvent findByNom ; cette
        // doublure n'attribue pas d'identifiant persistant.
        return Optional.empty();
    }

    @Override
    public List<ParametreScoring> findAll() {
        List<ParametreScoring> liste = new ArrayList<>();
        valeurs.forEach((nom, v) ->
                liste.add(new ParametreScoring(nom, v)));
        return liste;
    }

    @Override
    public ParametreScoring save(ParametreScoring parametre) {
        valeurs.put(parametre.getNom(), parametre.getValeur());
        return parametre;
    }

    private static BigDecimal valeur(String texte) {
        return new BigDecimal(texte);
    }
}
