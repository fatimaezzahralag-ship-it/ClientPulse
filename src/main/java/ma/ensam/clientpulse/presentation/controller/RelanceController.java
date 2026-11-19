package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.RelanceStatutRequest;
import ma.ensam.clientpulse.dto.ResultatTraitement;
import ma.ensam.clientpulse.service.interfaces.RelancePaiementService;
import ma.ensam.clientpulse.service.interfaces.RelanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relances")
@RequiredArgsConstructor
public class RelanceController {

    private final RelanceService relanceService;
    private final RelancePaiementService relancePaiementService;

    /** Exemple : GET /api/relances?nature=PAIEMENT&page=0&size=20 */
    @GetMapping
    public Page<RelanceDto> lister(@RequestParam(required = false) NatureRelance nature, Pageable pageable) {
        return relanceService.lister(nature, pageable);
    }

    @GetMapping("/{id}")
    public RelanceDto trouver(@PathVariable Long id) {
        return relanceService.trouver(id);
    }

    /** Marque une relance EN_COURS ou TERMINEE, avec le résultat de l'action. */
    @PatchMapping("/{id}/statut")
    public RelanceDto changerStatut(@PathVariable Long id, @Valid @RequestBody RelanceStatutRequest corps) {
        return relanceService.changerStatut(id, corps.statut(), corps.resultat());
    }

    /**
     * Lance à la main le traitement normalement fait chaque nuit (factures en retard + relances).
     * Utile pour la démo : POST /api/relances/paiement/generer?date=2026-12-20
     */
    @PostMapping("/paiement/generer")
    public ResultatTraitement generer(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return relancePaiementService.traiterJour(date != null ? date : LocalDate.now());
    }
}
