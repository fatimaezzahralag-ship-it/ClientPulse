package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.dto.ScoreClientDto;
import ma.ensam.clientpulse.service.interfaces.ChurnScoringService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expose le score de risque d'un client.
 *
 * GET  /api/clients/{id}/score       dernier score connu (204 si aucun)
 * POST /api/clients/{id}/score       recalcule et enregistre le score du jour
 * GET  /api/clients/{id}/score/historique
 */
@RestController
@RequestMapping("/api/clients/{clientId}/score")
public class ClientScoreController {

    private final ChurnScoringService churnScoringService;

    public ClientScoreController(ChurnScoringService churnScoringService) {
        this.churnScoringService = churnScoringService;
    }

    @GetMapping
    public ResponseEntity<ScoreClientDto> dernier(
            @PathVariable Long clientId) {

        return churnScoringService.dernierScore(clientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<ScoreClientDto> recalculer(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(
                churnScoringService.calculerPourClient(clientId)
        );
    }

    @GetMapping("/historique")
    public ResponseEntity<List<ScoreClientDto>> historique(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(
                churnScoringService.historique(clientId)
        );
    }
}
