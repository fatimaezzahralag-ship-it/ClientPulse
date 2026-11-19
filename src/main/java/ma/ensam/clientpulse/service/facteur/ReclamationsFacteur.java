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

/**
 * Évalue le nombre de réclamations encore ouvertes.
 *
 * Une réclamation close n'est plus un risque : seules les interactions de type
 * RECLAMATION non clôturées sont comptées par le DAO.
 */
@Service
public class ReclamationsFacteur implements FacteurRisque {

    public static final String CODE = "RECLAMATIONS";

    private final StatistiqueClientDao statistiqueClientDao;
    private final ParametresScoringReader parametres;

    public ReclamationsFacteur(
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

        long reclamations = stats != null && stats.reclamationsOuvertes() != null
                ? stats.reclamationsOuvertes()
                : 0L;

        if (reclamations == 0) {
            return new ResultatFacteur(
                    code(),
                    BigDecimal.ZERO,
                    "Aucune réclamation ouverte"
            );
        }

        int seuilMoyen = parametres.entier(
                NomsParametres.SEUIL_RECLAMATIONS_MOYEN);
        int seuilEleve = parametres.entier(
                NomsParametres.SEUIL_RECLAMATIONS_ELEVE);

        BigDecimal note;
        String qualificatif;

        if (reclamations <= seuilMoyen) {
            note = BigDecimal.valueOf(40);
            qualificatif = "risque modéré";
        } else if (reclamations <= seuilEleve) {
            note = BigDecimal.valueOf(70);
            qualificatif = "risque élevé";
        } else {
            note = BigDecimal.valueOf(100);
            qualificatif = "risque critique";
        }

        return new ResultatFacteur(
                code(),
                note,
                reclamations + " réclamation(s) ouverte(s) ("
                        + qualificatif + ")"
        );
    }
}
