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
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Compare les achats des 3 derniers mois à ceux des 3 mois précédents.
 *
 * Une chute durable du volume acheté est un signal fort de désengagement. Le
 * calcul ignore les factures annulées (fait par le DAO) et ne pénalise jamais
 * un client qui n'a pas d'historique de référence.
 */
@Service
public class BaisseAchatsFacteur implements FacteurRisque {

    public static final String CODE = "BAISSE_ACHATS";

    private final StatistiqueClientDao statistiqueClientDao;
    private final ParametresScoringReader parametres;

    public BaisseAchatsFacteur(
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

        BigDecimal recent = stats != null
                ? nul(stats.montantAchats3DerniersMois())
                : BigDecimal.ZERO;
        BigDecimal precedent = stats != null
                ? nul(stats.montantAchats3MoisPrecedents())
                : BigDecimal.ZERO;

        if (precedent.signum() == 0) {
            return new ResultatFacteur(
                    code(),
                    BigDecimal.ZERO,
                    recent.signum() == 0
                            ? "Aucun achat sur les deux périodes de référence"
                            : "Pas d'historique de référence : achats en hausse"
            );
        }

        BigDecimal baisse = precedent.subtract(recent)
                .divide(precedent, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .max(BigDecimal.ZERO);

        int seuilFaible = parametres.entier(
                NomsParametres.SEUIL_BAISSE_FAIBLE);
        int seuilMoyen = parametres.entier(
                NomsParametres.SEUIL_BAISSE_MOYEN);
        int seuilEleve = parametres.entier(
                NomsParametres.SEUIL_BAISSE_ELEVE);

        BigDecimal note;
        String qualificatif;

        if (baisse.doubleValue() < seuilFaible) {
            note = BigDecimal.ZERO;
            qualificatif = "stable ou en hausse";
        } else if (baisse.doubleValue() < seuilMoyen) {
            note = BigDecimal.valueOf(40);
            qualificatif = "baisse modérée";
        } else if (baisse.doubleValue() < seuilEleve) {
            note = BigDecimal.valueOf(70);
            qualificatif = "baisse importante";
        } else {
            note = BigDecimal.valueOf(100);
            qualificatif = "baisse très importante";
        }

        return new ResultatFacteur(
                code(),
                note,
                "Baisse des achats de "
                        + baisse.setScale(1, RoundingMode.HALF_UP)
                        + " % sur 3 mois (" + qualificatif + ")"
        );
    }

    private static BigDecimal nul(BigDecimal valeur) {
        return valeur != null ? valeur : BigDecimal.ZERO;
    }
}
