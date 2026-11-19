package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.TauxTvaDto;
import ma.ensam.clientpulse.service.interfaces.TauxTvaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Le controller ne fait que 3 choses : recevoir la requête HTTP, appeler le service, renvoyer le résultat.
 * Aucune règle métier ici.
 */
@RestController
@RequestMapping("/api/taux-tva")
@RequiredArgsConstructor
public class TauxTvaController {

    private final TauxTvaService service;   // l'interface, jamais l'Impl

    @GetMapping
    public List<TauxTvaDto> lister(@RequestParam(defaultValue = "false") boolean actifsSeulement) {
        return service.lister(actifsSeulement);
    }

    @GetMapping("/{id}")
    public TauxTvaDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TauxTvaDto creer(@Valid @RequestBody TauxTvaDto dto) {
        return service.creer(dto);
    }

    @PutMapping("/{id}")
    public TauxTvaDto modifier(@PathVariable Long id, @Valid @RequestBody TauxTvaDto dto) {
        return service.modifier(id, dto);
    }

    /** DELETE désactive le taux (il n'est pas effacé de la base). */
    @DeleteMapping("/{id}")
    public TauxTvaDto desactiver(@PathVariable Long id) {
        return service.desactiver(id);
    }
}
