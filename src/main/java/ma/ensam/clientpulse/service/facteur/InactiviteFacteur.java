package ma.ensam.clientpulse.service.facteur;

import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.FacteurRisque;
import ma.ensam.clientpulse.service.ResultatFacteur;
import ma.ensam.clientpulse.service.scoring.NomsParametres;
import ma.ensam.clientpulse.service.scoring.ParametresScoringReader;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mesure le temps écoulé depuis la dernière interaction du client.
 *
 * Plus le client est silencieux longtemps, plus le risque de churn est élevé.
 * Les seuils sont lus dans {@code parametres_scoring} (V4).
 */
@Service
public class InactiviteFacteur implements FacteurRisque {

    public static final String CODE = "INACTIVITE";

    private final StatistiqueClientDao statistiqueClientDao;
    private final ParametresScoringReader parametres;

    public InactiviteFacteur(
            StatistiqueClientDao statistiqueClientDao,
            ParametresScoringReader parametres) {
        this.statistiqueClientDao = statistiqueClientDao;
        this.parametres = parametres;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public ResultatFacteur evaluer(Client client, LocalDate jour) {

        StatistiqueClientDto stats = statistiqueClientDao
                .findStatistiquesByClientId(client.getId(), jour)
                .orElse(null);

        LocalDate derniereActivite =
                stats != null ? stats.derniereActivite() : null;

        if (derniereActivite == null) {
            return new ResultatFacteur(
                    code(),
                    BigDecimal.valueOf(100),
                    "Aucune activité enregistrée pour ce client"
            );
        }

        long jours = Math.max(
                0,
                ChronoUnit.DAYS.between(derniereActivite, jour)
        );

        int seuilFaible = parametres.entier(
                NomsParametres.SEUIL_INACTIVITE_FAIBLE);
        int seuilMoyen = parametres.entier(
                NomsParametres.SEUIL_INACTIVITE_MOYEN);
        int seuilEleve = parametres.entier(
                NomsParametres.SEUIL_INACTIVITE_ELEVE);

        BigDecimal note;
        String niveau;

        if (jours <= seuilFaible) {
            note = BigDecimal.ZERO;
            niveau = "activité récente";
        } else if (jours <= seuilMoyen) {
            note = BigDecimal.valueOf(40);
            niveau = "inactivité modérée";
        } else if (jours <= seuilEleve) {
            note = BigDecimal.valueOf(70);
            niveau = "inactivité importante";
        } else {
            note = BigDecimal.valueOf(100);
            niveau = "inactivité prolongée";
        }

        return new ResultatFacteur(
                code(),
                note,
                "Dernière activité il y a " + jours + " jours ("
                        + niveau + ")"
        );
    }
}
