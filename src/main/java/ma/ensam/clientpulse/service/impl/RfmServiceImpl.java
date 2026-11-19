package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.dao.interfaces.StatistiqueClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.SegmentRFM;
import ma.ensam.clientpulse.dto.StatistiqueClientDto;
import ma.ensam.clientpulse.service.interfaces.RfmService;
import ma.ensam.clientpulse.service.scoring.NomsParametres;
import ma.ensam.clientpulse.service.scoring.ParametresScoringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Segmentation RFM.
 *
 * Les trois indicateurs sont lus uniquement depuis {@link StatistiqueClientDao}
 * (jamais depuis FactureDao). Les seuils sont lus dans parametres_scoring
 * (migration V5) : aucun chiffre n'est codé en dur ici.
 */
@Service
public class RfmServiceImpl implements RfmService {

    private static final Logger LOG = LoggerFactory.getLogger(RfmServiceImpl.class);

    private static final int TAILLE_LOT = 200;

    private final StatistiqueClientDao statistiqueClientDao;
    private final ParametresScoringReader parametres;
    private final ClientDao clientDao;
    private final ScoreClientDao scoreClientDao;

    public RfmServiceImpl(
            StatistiqueClientDao statistiqueClientDao,
            ParametresScoringReader parametres,
            ClientDao clientDao,
            ScoreClientDao scoreClientDao) {
        this.statistiqueClientDao = statistiqueClientDao;
        this.parametres = parametres;
        this.clientDao = clientDao;
        this.scoreClientDao = scoreClientDao;
    }

    @Override
    public SegmentRFM segmenter(Client client, LocalDate jour) {

        StatistiqueClientDto stats = statistiqueClientDao
                .findStatistiquesByClientId(client.getId(), jour)
                .orElse(null);

        // Sans historique d'achat : l'activité récente distingue un nouveau
        // client d'un client perdu. On n'invente jamais de segment "inconnu".
        if (stats == null || stats.dernierAchat() == null) {
            long joursDepuisActivite = stats != null
                    && stats.derniereActivite() != null
                    ? joursDepuis(stats.derniereActivite(), jour)
                    : Long.MAX_VALUE;
            return joursDepuisActivite <= parametres.entier(
                    NomsParametres.RFM_NOUVEAU_JOURS)
                    ? SegmentRFM.NOUVEAU
                    : SegmentRFM.PERDU;
        }

        long recence = joursDepuis(stats.dernierAchat(), jour);

        if (recence >= parametres.entier(NomsParametres.RFM_PERDU_JOURS)) {
            return SegmentRFM.PERDU;
        }
        if (recence >= parametres.entier(
                NomsParametres.RFM_HIBERNATION_JOURS)) {
            return SegmentRFM.HIBERNATION;
        }
        if (stats.premierAchat() != null
                && joursDepuis(stats.premierAchat(), jour)
                <= parametres.entier(NomsParametres.RFM_NOUVEAU_JOURS)) {
            return SegmentRFM.NOUVEAU;
        }

        long frequence = stats.nombreAchats12Mois() != null
                ? stats.nombreAchats12Mois()
                : 0L;
        BigDecimal montant = stats.montantAchats12Mois() != null
                ? stats.montantAchats12Mois()
                : BigDecimal.ZERO;

        if (recence <= parametres.entier(NomsParametres.RFM_RECENCE_CHAMPION)
                && frequence >= parametres.entier(
                        NomsParametres.RFM_FREQUENCE_CHAMPION)
                && montant.compareTo(parametres.valeur(
                        NomsParametres.RFM_MONETAIRE_CHAMPION)) >= 0) {
            return SegmentRFM.CHAMPION;
        }

        if (recence <= parametres.entier(NomsParametres.RFM_RECENCE_LOYAL)
                && frequence >= parametres.entier(
                        NomsParametres.RFM_FREQUENCE_LOYAL)
                && montant.compareTo(parametres.valeur(
                        NomsParametres.RFM_MONETAIRE_LOYAL)) >= 0) {
            return SegmentRFM.LOYAL;
        }

        return SegmentRFM.A_RISQUE;
    }

    @Override
    public void segmenter(LocalDate jour) {

        int numeroLot = 0;
        int segmentsModifies = 0;

        while (true) {
            Page<Client> lot = clientDao.search(
                    null, null, null, null, null,
                    PageRequest.of(
                            numeroLot,
                            TAILLE_LOT,
                            Sort.by(Sort.Direction.ASC, "id")
                    )
            );

            for (Client client : lot.getContent()) {
                try {
                    SegmentRFM segment = segmenter(client, jour);
                    var score = scoreClientDao.findDernierByClientId(
                            client.getId());
                    if (score.isPresent()
                            && score.get().getSegment() != segment) {
                        ScoreClient scoreClient = score.get();
                        scoreClient.setSegment(segment);
                        scoreClientDao.save(scoreClient);
                        segmentsModifies++;
                    }
                } catch (RuntimeException erreur) {
                    // Un client avec des statistiques incohérentes ne doit pas
                    // empêcher la segmentation des autres clients du lot.
                    LOG.warn(
                            "Segmentation RFM impossible pour le client {} : {}",
                            client.getId(), erreur.getMessage()
                    );
                }
            }

            if (lot.isLast() || lot.isEmpty()) {
                break;
            }
            numeroLot++;
        }

        LOG.info(
                "Segmentation RFM du {} terminée : {} segment(s) mis à jour",
                jour, segmentsModifies
        );
    }

    private static long joursDepuis(LocalDate date, LocalDate jour) {
        return Math.max(0, ChronoUnit.DAYS.between(date, jour));
    }
}
