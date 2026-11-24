package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ParametreScoringDao;
import ma.ensam.clientpulse.domain.entity.ParametreScoring;
import ma.ensam.clientpulse.dto.ParametreScoringDto;
import ma.ensam.clientpulse.dto.ParametreScoringRequest;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.ParametreScoringService;
import ma.ensam.clientpulse.service.scoring.NomsParametres;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applique les règles métier avant toute modification d'un paramètre.
 *
 * Les contrôles sont faits sur une copie en mémoire de la table complète :
 * on valide donc la cohérence GLOBALE (somme des poids, ordre des seuils),
 * pas seulement la valeur isolée.
 */
@Service
@Transactional
public class ParametreScoringServiceImpl
        implements ParametreScoringService {

    private static final BigDecimal UN = BigDecimal.ONE;
    private static final BigDecimal CENT = BigDecimal.valueOf(100);
    private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");

    private final ParametreScoringDao parametreScoringDao;

    public ParametreScoringServiceImpl(
            ParametreScoringDao parametreScoringDao) {
        this.parametreScoringDao = parametreScoringDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametreScoringDto> findAll() {
        return parametreScoringDao.findAll().stream()
                .map(ParametreScoringServiceImpl::versDto)
                .toList();
    }

    @Override
    public ParametreScoringDto modifier(
            Long id, ParametreScoringRequest request) {

        ParametreScoring parametre = parametreScoringDao.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Paramètre introuvable : " + id));

        if (request.nom() != null && !request.nom().isBlank()
                && !request.nom().equals(parametre.getNom())) {
            throw new RegleMetierException(
                    "Le nom d'un paramètre ne peut pas être modifié");
        }
        if (request.valeur() == null
                || request.valeur().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegleMetierException(
                    "La valeur d'un paramètre doit être positive");
        }

        Map<String, BigDecimal> valeurs = new LinkedHashMap<>();
        for (ParametreScoring p : parametreScoringDao.findAll()) {
            valeurs.put(p.getNom(), p.getValeur());
        }
        valeurs.put(parametre.getNom(), request.valeur());

        verifierCoherence(valeurs);

        parametre.setValeur(request.valeur());
        return versDto(parametreScoringDao.save(parametre));
    }

    private static void verifierCoherence(Map<String, BigDecimal> v) {
        BigDecimal sommePoids = BigDecimal.ZERO;
        for (String nom : List.of(
                NomsParametres.POIDS_INACTIVITE,
                NomsParametres.POIDS_BAISSE_ACHATS,
                NomsParametres.POIDS_RECLAMATIONS,
                NomsParametres.POIDS_RETARD_PAIEMENT)) {
            BigDecimal poids = v.get(nom);
            if (poids == null || poids.signum() < 0) {
                throw new RegleMetierException(
                        "Chaque poids doit être défini et positif");
            }
            sommePoids = sommePoids.add(poids);
        }
        if (sommePoids.subtract(UN).abs().compareTo(TOLERANCE) > 0) {
            throw new RegleMetierException(
                    "La somme des poids des facteurs doit valoir 1");
        }

        ordreCroissant(v, "seuils de risque",
                NomsParametres.SEUIL_RISQUE_MOYEN,
                NomsParametres.SEUIL_RISQUE_ELEVE,
                NomsParametres.SEUIL_RISQUE_CRITIQUE);
        borne(v, NomsParametres.SEUIL_RISQUE_CRITIQUE, CENT);

        ordreCroissant(v, "seuils d'inactivité",
                NomsParametres.SEUIL_INACTIVITE_FAIBLE,
                NomsParametres.SEUIL_INACTIVITE_MOYEN,
                NomsParametres.SEUIL_INACTIVITE_ELEVE);
        ordreCroissant(v, "seuils de baisse des achats",
                NomsParametres.SEUIL_BAISSE_FAIBLE,
                NomsParametres.SEUIL_BAISSE_MOYEN,
                NomsParametres.SEUIL_BAISSE_ELEVE);
        ordreCroissant(v, "seuils de réclamations",
                NomsParametres.SEUIL_RECLAMATIONS_MOYEN,
                NomsParametres.SEUIL_RECLAMATIONS_ELEVE);
        ordreCroissant(v, "seuils de retard de paiement",
                NomsParametres.SEUIL_RETARD_FAIBLE,
                NomsParametres.SEUIL_RETARD_MOYEN);

        ordreCroissant(v, "seuils de récence RFM",
                NomsParametres.RFM_RECENCE_CHAMPION,
                NomsParametres.RFM_RECENCE_LOYAL,
                NomsParametres.RFM_HIBERNATION_JOURS,
                NomsParametres.RFM_PERDU_JOURS);
        ordreCroissant(v, "seuil de nouveau client et d'hibernation",
                NomsParametres.RFM_NOUVEAU_JOURS,
                NomsParametres.RFM_HIBERNATION_JOURS);
        ordreDecroissant(v, "seuils de fréquence RFM",
                NomsParametres.RFM_FREQUENCE_CHAMPION,
                NomsParametres.RFM_FREQUENCE_LOYAL);
        ordreDecroissant(v, "seuils monétaires RFM",
                NomsParametres.RFM_MONETAIRE_CHAMPION,
                NomsParametres.RFM_MONETAIRE_LOYAL);

        // Délais des relances de paiement : J+3 < J+15 < J+30 < J+45 (le délai
        // préventif N1 est un nombre de jours AVANT l'échéance, sans ordre à respecter).
        ordreCroissant(v, "délais de relance de paiement",
                NomsParametres.DELAI_RELANCE_N2,
                NomsParametres.DELAI_RELANCE_N3,
                NomsParametres.DELAI_RELANCE_N4,
                NomsParametres.DELAI_RELANCE_N5);
    }

    private static void ordreCroissant(
            Map<String, BigDecimal> valeurs, String libelle, String... noms) {
        verifierOrdre(valeurs, libelle, true, noms);
    }

    private static void ordreDecroissant(
            Map<String, BigDecimal> valeurs, String libelle, String... noms) {
        verifierOrdre(valeurs, libelle, false, noms);
    }

    private static void verifierOrdre(
            Map<String, BigDecimal> valeurs,
            String libelle,
            boolean croissant,
            String... noms) {

        for (int i = 0; i < noms.length - 1; i++) {
            BigDecimal gauche = valeurs.get(noms[i]);
            BigDecimal droite = valeurs.get(noms[i + 1]);
            if (gauche == null || droite == null) {
                throw new RegleMetierException(
                        "Paramètre manquant pour valider les " + libelle);
            }
            int comparaison = gauche.compareTo(droite);
            boolean invalide = croissant ? comparaison >= 0 : comparaison <= 0;
            if (invalide) {
                throw new RegleMetierException(
                        "Les " + libelle + " ne sont pas cohérents");
            }
        }
    }

    private static void borne(
            Map<String, BigDecimal> valeurs, String nom, BigDecimal maximum) {
        BigDecimal valeur = valeurs.get(nom);
        if (valeur != null && valeur.compareTo(maximum) > 0) {
            throw new RegleMetierException(
                    "Le paramètre " + nom + " ne peut pas dépasser " + maximum);
        }
    }

    private static ParametreScoringDto versDto(ParametreScoring parametre) {
        return new ParametreScoringDto(
                parametre.getId(),
                parametre.getNom(),
                parametre.getValeur()
        );
    }
}
