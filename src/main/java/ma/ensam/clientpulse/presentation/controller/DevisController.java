package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.DevisDto;
import ma.ensam.clientpulse.service.interfaces.DevisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devis")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService service;

    @GetMapping
    public Page<DevisDto> lister(Pageable pageable) {
        return service.lister(pageable);
    }

    @GetMapping("/{id}")
    public DevisDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DevisDto creer(@Valid @RequestBody DevisDto dto) {
        return service.creer(dto);
    }

    @PutMapping("/{id}")
    public DevisDto modifier(@PathVariable Long id, @Valid @RequestBody DevisDto dto) {
        return service.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    // Les changements de statut sont des ACTIONS : on les nomme dans l'URL

    @PostMapping("/{id}/envoi")
    public DevisDto envoyer(@PathVariable Long id) {
        return service.envoyer(id);
    }

    @PostMapping("/{id}/acceptation")
    public DevisDto accepter(@PathVariable Long id) {
        return service.accepter(id);
    }

    @PostMapping("/{id}/refus")
    public DevisDto refuser(@PathVariable Long id) {
        return service.refuser(id);
    }
}
