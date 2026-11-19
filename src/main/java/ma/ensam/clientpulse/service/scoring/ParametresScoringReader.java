package ma.ensam.clientpulse.service.scoring;

import ma.ensam.clientpulse.dao.interfaces.ParametreScoringDao;
import ma.ensam.clientpulse.domain.entity.ParametreScoring;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Lecture typée des paramètres de scoring.
 *
 * Les facteurs ne connaissent pas les noms exacts des paramètres : ils passent
 * par ce lecteur, qui échoue clairement si la V4 n'a pas été appliquée ou si un
 * paramètre a été supprimé.
 */
@Component
public class ParametresScoringReader {

    private final ParametreScoringDao parametreScoringDao;

    public ParametresScoringReader(
            ParametreScoringDao parametreScoringDao) {
        this.parametreScoringDao = parametreScoringDao;
    }

    public BigDecimal valeur(String nom) {
        return parametreScoringDao.findByNom(nom)
                .map(ParametreScoring::getValeur)
                .orElseThrow(() -> new IllegalStateException(
                        "Paramètre de scoring manquant : " + nom));
    }

    public int entier(String nom) {
        return valeur(nom).intValue();
    }
}
