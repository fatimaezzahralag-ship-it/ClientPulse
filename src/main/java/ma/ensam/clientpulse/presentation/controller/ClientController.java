package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;

import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.dto.ClientDto;
import ma.ensam.clientpulse.dto.ClientRequest;
import ma.ensam.clientpulse.service.interfaces.ClientService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientDto> create(
            @Valid @RequestBody ClientRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(clientService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
            clientService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<ClientDto>> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StatutClient statut,
            @RequestParam(required = false) TypeClient type,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Long commercialId,
            @PageableDefault(size = 10, sort = "raisonSociale")
            Pageable pageable) {

        return ResponseEntity.ok(
            clientService.search(
                keyword,
                statut,
                type,
                ville,
                commercialId,
                pageable
            )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {

        return ResponseEntity.ok(
            clientService.update(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id) {

        clientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
