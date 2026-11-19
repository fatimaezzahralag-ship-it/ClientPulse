package ma.ensam.clientpulse.service.job;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.service.interfaces.ChurnScoringService;
import ma.ensam.clientpulse.service.interfaces.RelanceCommercialService;
import ma.ensam.clientpulse.service.interfaces.RfmService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Recalcul nocturne des scores, des segments RFM et des relances commerciales.
 *
 * Les clients sont parcourus par lots pour ne jamais charger 5 000 fiches en
 * mémoire. Une erreur sur un client est journalisée puis ignorée : le reste du
 * lot est traité. Un verrou atomique empêche deux exécutions concurrentes
 * (déclenchement cron + recalcul manuel), source de doublons de relances.
 */
@Component
public class ScoringJob {

    private static final Logger LOG = LoggerFactory.getLogger(ScoringJob.class);

    private static final int TAILLE_LOT = 100;

    private final ChurnScoringService churnScoringService;
    private final RfmService rfmService;
    private final RelanceCommercialService relanceCommercialService;
    private final ClientDao clientDao;
    private final AtomicBoolean enCours = new AtomicBoolean(false);

    @Value("${app.scoring.relances.enabled:true}")
    private boolean generationRelancesActivee;

    public ScoringJob(
            ChurnScoringService churnScoringService,
            RfmService rfmService,
            RelanceCommercialService relanceCommercialService,
            ClientDao clientDao) {
        this.churnScoringService = churnScoringService;
        this.rfmService = rfmService;
        this.relanceCommercialService = relanceCommercialService;
        this.clientDao = clientDao;
    }

    /** Exécution automatique, cron configurable dans application.properties. */
    @Scheduled(cron = "${app.scoring.cron:0 0 2 * * *}")
    public void recalculerScores() {
        executer();
    }

    /**
     * Exécute un recalcul complet. Renvoie false si un recalcul est déjà en
     * cours, true lorsque le traitement a été mené à son terme.
     */
    public boolean executer() {
        if (!enCours.compareAndSet(false, true)) {
            LOG.warn("Un recalcul des scores est déjà en cours : exécution ignorée");
            return false;
        }

        LocalDate jour = LocalDate.now();
        long debut = System.currentTimeMillis();

        try {
            int clientsTraites = 0;
            int clientsEnErreur = 0;
            int numeroLot = 0;

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
                        churnScoringService.calculer(client, jour);
                        clientsTraites++;
                    } catch (RuntimeException erreur) {
                        clientsEnErreur++;
                        LOG.warn(
                                "Score impossible pour le client {} : {}",
                                client.getId(), erreur.getMessage()
                        );
                    }
                }

                if (lot.isLast() || lot.isEmpty()) {
                    break;
                }
                numeroLot++;
            }

            // Rafraîchit les segments déjà stockés (idempotent) : utile quand
            // les seuils RFM ont été modifiés sans recalcul complet.
            try {
                rfmService.segmenter(jour);
            } catch (RuntimeException erreur) {
                LOG.error("Échec de la segmentation RFM globale", erreur);
            }

            int relances = 0;
            if (generationRelancesActivee) {
                try {
                    relances = relanceCommercialService.genererRelances(jour);
                } catch (RuntimeException erreur) {
                    LOG.error("Échec de la génération des relances", erreur);
                }
            }

            LOG.info(
                    "Recalcul du {} terminé en {} ms : {} score(s), "
                            + "{} erreur(s), {} relance(s) créée(s)",
                    jour,
                    System.currentTimeMillis() - debut,
                    clientsTraites,
                    clientsEnErreur,
                    relances
            );
            return true;
        } finally {
            enCours.set(false);
        }
    }
}
