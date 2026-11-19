package ma.ensam.clientpulse.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.interfaces.RelancePaiementService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Tâche planifiée de nuit. Elle ne contient AUCUNE règle : elle appelle le service.
 * (Le même service est utilisable à la main via POST /api/relances/paiement/generer, pratique pour la démo.)
 * Il faut @EnableScheduling : c'est le rôle de config/SchedulingConfig.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RelancePaiementJob {

    private final RelancePaiementService relancePaiementService;

    /** Chaque nuit à 02:00, heure du Maroc. Format cron : seconde minute heure jour mois jour-de-semaine. */
    @Scheduled(cron = "0 0 2 * * *", zone = "Africa/Casablanca")
    public void executer() {
        ResultatTraitement bilan = relancePaiementService.traiterJour(LocalDate.now());
        log.info("Traitement de nuit : {} facture(s) passée(s) en retard, {} relance(s) créée(s)",
                bilan.facturesPasseesEnRetard(), bilan.relancesCreees());
    }
}
