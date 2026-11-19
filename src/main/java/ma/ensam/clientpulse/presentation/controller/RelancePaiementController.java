package ma.ensam.clientpulse.presentation.controller;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.interfaces.RelancePaiementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Relances de paiement (P1). Le suivi des relances (liste, démarrer, traiter) reste dans RelanceController de P2 ;
 * ce contrôleur ne sert qu'à lancer le traitement à la main, utile pour la démonstration.
 */
@RestController
@RequestMapping("/api/relances/paiement")
@RequiredArgsConstructor
public class RelancePaiementController {

    private final RelancePaiementService service;

    /** Exemple : POST /api/relances/paiement/generer?date=2026-12-20 (sans date : aujourd'hui). */
    @PostMapping("/generer")
    public ResultatTraitement generer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.traiterJour(date != null ? date : LocalDate.now());
    }
}
