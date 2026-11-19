package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;

import ma.ensam.clientpulse.dto.ParametreScoringDto;
import ma.ensam.clientpulse.dto.ParametreScoringRequest;
import ma.ensam.clientpulse.service.interfaces.ParametreScoringService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Paramètres du moteur de scoring.
 *
 * GET /api/parametres        : ADMIN, MANAGER, ANALYSTE
 * PUT /api/parametres/{id}   : ADMIN uniquement (voir SecurityConfig)
 */
@RestController
@RequestMapping("/api/parametres")
public class ParametreScoringController {

    private final ParametreScoringService parametreScoringService;

    public ParametreScoringController(
            ParametreScoringService parametreScoringService) {
        this.parametreScoringService = parametreScoringService;
    }

    @GetMapping
    public ResponseEntity<List<ParametreScoringDto>> findAll() {
        return ResponseEntity.ok(parametreScoringService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametreScoringDto> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ParametreScoringRequest request) {
        return ResponseEntity.ok(
                parametreScoringService.modifier(id, request));
    }
}
