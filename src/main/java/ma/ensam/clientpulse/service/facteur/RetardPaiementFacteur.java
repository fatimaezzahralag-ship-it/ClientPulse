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
 * Analyse le retard moyen de paiement fourni par le DAO.
 *
 * Le DAO prend en compte les paiements partiels et les factures encore
 * ouvertes : un client qui règle toujours à temps obtient 0, un client qui paie
 * avec plus de 30 jours de retard obtient la note maximale.
 */
@Service
public class RetardPaiementFacteur implements FacteurRisque {

    public static final String CODE = "RETARD_PAIEMENT";

    private final StatistiqueClientDao statistiqueClientDao;
    private final ParametresScoringReader parametres;

    public RetardPaiementFacteur(
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

        BigDecimal retard = stats != null
                && stats.retardMoyenPaiement() != null
                ? stats.retardMoyenPaiement()
                : BigDecimal.ZERO;

        if (retard.signum() <= 0) {
            return new ResultatFacteur(
                    code(),
                    BigDecimal.ZERO,
                    "Aucun retard de paiement constaté"
            );
        }

        int seuilFaible = parametres.entier(
                NomsParametres.SEUIL_RETARD_FAIBLE);
        int seuilMoyen = parametres.entier(
                NomsParametres.SEUIL_RETARD_MOYEN);

        BigDecimal note;
        String qualificatif;

        if (retard.doubleValue() <= seuilFaible) {
            note = BigDecimal.valueOf(20);
            qualificatif = "retard faible";
        } else if (retard.doubleValue() <= seuilMoyen) {
            note = BigDecimal.valueOf(50);
            qualificatif = "retard modéré";
        } else {
            note = BigDecimal.valueOf(90);
            qualificatif = "retard important";
        }

        return new ResultatFacteur(
                code(),
                note,
                "Retard moyen de paiement de "
                        + retard.setScale(1, RoundingMode.HALF_UP)
                        + " jours (" + qualificatif + ")"
        );
    }
}
