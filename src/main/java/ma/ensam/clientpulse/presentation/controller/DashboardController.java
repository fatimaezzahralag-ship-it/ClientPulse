package ma.ensam.clientpulse.presentation.controller;

import ma.ensam.clientpulse.dto.DashboardDto;
import ma.ensam.clientpulse.service.interfaces.DashboardService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Tableau de bord financier (ADMIN, MANAGER, ANALYSTE : voir SecurityConfig).
 *
 * GET /api/dashboard?periode=12&date=2026-11-19   indicateurs (périodes de 1 à 12 mois)
 * GET /api/dashboard/export?periode=12            export CSV des indicateurs
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDto indicateurs(
            @RequestParam(defaultValue = "12") int periode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dashboardService.calculer(periode, date != null ? date : LocalDate.now());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exporter(
            @RequestParam(defaultValue = "12") int periode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate jour = date != null ? date : LocalDate.now();
        byte[] contenu = dashboardService.exporterCsv(periode, jour).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("tableau-de-bord-" + jour + ".csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenu);
    }
}
