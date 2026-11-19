package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ScoreClientDao;
import ma.ensam.clientpulse.domain.entity.ScoreClient;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.ResultatRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.RelanceRequest;
import ma.ensam.clientpulse.service.interfaces.RelanceCommercialService;
import ma.ensam.clientpulse.service.interfaces.RelanceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Génère les relances commerciales à partir des derniers scores à risque.
 *
 * La nature est toujours COMMERCIALE : les relances de paiement (P1) ne sont
 * pas concernées. L'anti-doublon et l'affectation sont assurés par
 * {@link RelanceService}, contrat commun partagé avec P1.
 */
@Service
@Transactional
public class RelanceCommercialServiceImpl
        implements RelanceCommercialService {

    private static final Logger LOG =
            LoggerFactory.getLogger(RelanceCommercialServiceImpl.class);

    private final ScoreClientDao scoreClientDao;
    private final RelanceService relanceService;

    public RelanceCommercialServiceImpl(
            ScoreClientDao scoreClientDao,
            RelanceService relanceService) {
        this.scoreClientDao = scoreClientDao;
        this.relanceService = relanceService;
    }

    @Override
    public int genererRelances(LocalDate jour) {
        int creees = 0;

        for (ScoreClient score : scoreClientDao.findDerniersScoresARelancer()) {
            Long clientId = score.getClient() != null
                    ? score.getClient().getId()
                    : null;
            if (clientId == null) {
                continue;
            }

            try {
                Optional<RelanceDto> existante = relanceService.relanceActive(
                        clientId, NatureRelance.COMMERCIALE);
                if (existante.isPresent()) {
                    continue;
                }

                relanceService.creer(new RelanceRequest(
                        clientId,
                        null,
                        NatureRelance.COMMERCIALE,
                        motif(score),
                        null
                ));
                creees++;
            } catch (RuntimeException erreur) {
                // Une génération ratée pour un client ne bloque pas les autres.
                LOG.warn(
                        "Relance commerciale impossible pour le client {} : {}",
                        clientId, erreur.getMessage()
                );
            }
        }

        LOG.info(
                "Génération des relances commerciales du {} : {} créée(s)",
                jour, creees
        );
        return creees;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelanceDto> rechercherRelances(
            Long commercialId, StatutRelance statut, Pageable pageable) {
        return relanceService.rechercher(commercialId, statut, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelanceDto> findById(Long relanceId) {
        return relanceService.findById(relanceId);
    }

    @Override
    public RelanceDto demarrerRelance(Long relanceId) {
        return relanceService.demarrer(relanceId);
    }

    @Override
    public void traiterRelance(
            Long relanceId, ResultatRelance resultat, String commentaire) {
        relanceService.traiter(relanceId, resultat, commentaire);
    }

    private static String motif(ScoreClient score) {
        StringBuilder texte = new StringBuilder()
                .append("Risque ")
                .append(score.getNiveau())
                .append(" (score ")
                .append(score.getScoreRisque())
                .append("/100)")
                .append(" - segment ")
                .append(score.getSegment());

        if (score.getFacteursExplicatifs() != null
                && !score.getFacteursExplicatifs().isBlank()) {
            texte.append(". Facteurs : ")
                    .append(score.getFacteursExplicatifs().replace('\n', ' '));
        }

        return texte.toString();
    }
}
