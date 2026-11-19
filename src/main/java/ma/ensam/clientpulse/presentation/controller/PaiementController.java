package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import ma.ensam.clientpulse.service.interfaces.PaiementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Un paiement n'existe que sur une facture : l'URL est imbriquée sous /api/factures/{id}. */
@RestController
@RequestMapping("/api/factures/{factureId}/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService service;

    @GetMapping
    public List<PaiementDto> lister(@PathVariable Long factureId) {
        return service.lister(factureId);
    }

    /** Renvoie la facture mise à jour (nouveau statut, reste à payer). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FactureDto enregistrer(@PathVariable Long factureId, @Valid @RequestBody PaiementDto dto) {
        return service.enregistrer(factureId, dto);
    }
}
