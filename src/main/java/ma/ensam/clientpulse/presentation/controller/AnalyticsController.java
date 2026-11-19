package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.domain.enums.NiveauRisque;
import ma.ensam.clientpulse.dto.AnalyticsDashboardDto;
import ma.ensam.clientpulse.dto.ClientARelancerDto;
import ma.ensam.clientpulse.dto.ScoreClientDto;
import ma.ensam.clientpulse.service.interfaces.AnalyticsService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API analytique (ADMIN, MANAGER, ANALYSTE).
 *
 * GET  /api/analytics/dashboard
 * GET  /api/analytics/scores
 * GET  /api/analytics/segments
 * GET  /api/analytics/niveaux
 * GET  /api/analytics/clients-a-relancer
 * GET  /api/analytics/clients/{id}/score
 * POST /api/analytics/recalculer
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardDto> dashboard() {
        return ResponseEntity.ok(analyticsService.dashboard());
    }

    @GetMapping("/scores")
    public ResponseEntity<Page<ScoreClientDto>> scores(
            @RequestParam(required = false) NiveauRisque niveau,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(analyticsService.scores(niveau, pageable));
    }

    @GetMapping("/segments")
    public ResponseEntity<Map<String, Long>> segments() {
        return ResponseEntity.ok(analyticsService.repartitionSegments());
    }

    @GetMapping("/niveaux")
    public ResponseEntity<Map<String, Long>> niveaux() {
        return ResponseEntity.ok(analyticsService.repartitionNiveaux());
    }

    @GetMapping("/clients-a-relancer")
    public ResponseEntity<Page<ClientARelancerDto>> clientsARelancer(
            @RequestParam(required = false) NiveauRisque niveau,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                analyticsService.clientsARelancer(niveau, pageable));
    }

    @GetMapping("/clients/{clientId}/score")
    public ResponseEntity<ScoreClientDto> scoreClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(analyticsService.scoreClient(clientId));
    }

    @PostMapping("/recalculer")
    public ResponseEntity<Void> recalculer() {
        return analyticsService.declencherRecalcul()
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
