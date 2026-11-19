package ma.ensam.clientpulse.presentation.controller;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.domain.enums.StatutFacture;
import ma.ensam.clientpulse.dto.EmissionFactureRequest;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.service.interfaces.FacturationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FacturationService service;

    /** Exemple : GET /api/factures?statut=EN_RETARD&page=0&size=20 */
    @GetMapping
    public Page<FactureDto> lister(@RequestParam(required = false) StatutFacture statut, Pageable pageable) {
        return service.lister(statut, pageable);
    }

    @GetMapping("/{id}")
    public FactureDto trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    /** Conversion d'un devis accepté en facture brouillon. */
    @PostMapping("/depuis-devis/{devisId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FactureDto creerDepuisDevis(@PathVariable Long devisId) {
        return service.creerDepuisDevis(devisId);
    }

    /** Le corps est facultatif : sans lui, émission aujourd'hui et échéance à 30 jours. */
    @PostMapping("/{id}/emission")
    public FactureDto emettre(@PathVariable Long id,
                              @RequestBody(required = false) EmissionFactureRequest corps) {
        if (corps == null) {
            return service.emettre(id, null, null);
        }
        return service.emettre(id, corps.dateEmission(), corps.dateEcheance());
    }

    @PostMapping("/{id}/annulation")
    public FactureDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}
