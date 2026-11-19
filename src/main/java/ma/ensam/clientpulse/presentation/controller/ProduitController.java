package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.ProduitDto;
import ma.ensam.clientpulse.service.interfaces.ProduitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService service;

    /** Exemple : GET /api/produits?q=licence&page=0&size=10 */
    @GetMapping
    public Page<ProduitDto> lister(@RequestParam(required = false) String q, Pageable pageable) {
        return service.lister(q, pageable);
    }

    @GetMapping("/{id}")
    public ProduitDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitDto creer(@Valid @RequestBody ProduitDto dto) {
        return service.creer(dto);
    }

    @PutMapping("/{id}")
    public ProduitDto modifier(@PathVariable Long id, @Valid @RequestBody ProduitDto dto) {
        return service.modifier(id, dto);
    }

    /** DELETE désactive le produit (il reste dans les anciens devis et factures). */
    @DeleteMapping("/{id}")
    public ProduitDto desactiver(@PathVariable Long id) {
        return service.desactiver(id);
    }
}
