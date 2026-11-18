package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;

import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;
import ma.ensam.clientpulse.dto.InteractionDto;
import ma.ensam.clientpulse.dto.InteractionRequest;
import ma.ensam.clientpulse.service.interfaces.InteractionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/clients/{clientId}/interactions")
    public ResponseEntity<InteractionDto> ajouter(
            @PathVariable Long clientId,
            @Valid @RequestBody InteractionRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(interactionService.ajouter(clientId, request));
    }

    @GetMapping("/clients/{clientId}/interactions")
    public ResponseEntity<Page<InteractionDto>> listerParClient(
            @PathVariable Long clientId,
            @RequestParam(required = false) TypeInteraction type,
            @RequestParam(required = false) StatutInteraction statut,
            @PageableDefault(
                size = 10,
                sort = "date",
                direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
            interactionService.listerParClient(
                clientId,
                type,
                statut,
                pageable
            )
        );
    }

    @PatchMapping("/interactions/{id}/statut")
    public ResponseEntity<InteractionDto> changerStatut(
            @PathVariable Long id,
            @RequestParam String statut) {

        return ResponseEntity.ok(
            interactionService.changerStatut(id, statut)
        );
    }
}
