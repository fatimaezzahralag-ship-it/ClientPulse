package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;

import ma.ensam.clientpulse.dto.UtilisateurDto;
import ma.ensam.clientpulse.dto.UtilisateurRequest;
import ma.ensam.clientpulse.service.interfaces.UtilisateurService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurDto>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDto> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
            utilisateurService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UtilisateurDto> create(
            @Valid @RequestBody UtilisateurRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(utilisateurService.create(request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id) {

        utilisateurService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
