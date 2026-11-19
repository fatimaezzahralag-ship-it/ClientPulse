package ma.ensam.clientpulse.presentation.controller;

import jakarta.validation.Valid;

import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import ma.ensam.clientpulse.dto.ResultatRelanceRequest;
import ma.ensam.clientpulse.service.interfaces.CommercialService;
import ma.ensam.clientpulse.service.interfaces.RelanceCommercialService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/** Relances commerciales : consultation, statut et résultat. */
@RestController
@RequestMapping("/api/relances")
public class RelanceController {

    private final RelanceCommercialService relanceCommercialService;
    private final CommercialService commercialService;

    public RelanceController(
            RelanceCommercialService relanceCommercialService,
            CommercialService commercialService) {
        this.relanceCommercialService = relanceCommercialService;
        this.commercialService = commercialService;
    }

    @GetMapping
    public ResponseEntity<Page<RelanceDto>> rechercher(
            @RequestParam(required = false) Long commercialId,
            @RequestParam(required = false) StatutRelance statut,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {

        return ResponseEntity.ok(
                relanceCommercialService.rechercherRelances(
                        identifiantCommercial(
                                commercialId, authentication),
                        statut,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelanceDto> findById(
            @PathVariable Long id, Authentication authentication) {

        return relanceCommercialService.findById(id)
                .filter(relance -> accessible(relance, authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<RelanceDto> demarrer(
            @PathVariable Long id, Authentication authentication) {

        Optional<RelanceDto> existante = relanceCommercialService.findById(id);
        if (existante.isEmpty()
                || !accessible(existante.get(), authentication)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                relanceCommercialService.demarrerRelance(id));
    }

    @PostMapping("/{id}/traiter")
    public ResponseEntity<RelanceDto> traiter(
            @PathVariable Long id,
            @Valid @RequestBody ResultatRelanceRequest request,
            Authentication authentication) {

        Optional<RelanceDto> existante = relanceCommercialService.findById(id);
        if (existante.isEmpty()
                || !accessible(existante.get(), authentication)) {
            return ResponseEntity.notFound().build();
        }

        relanceCommercialService.traiterRelance(
                id, request.resultat(), request.commentaire());
        return ResponseEntity.ok(
                relanceCommercialService.findById(id).orElseThrow()
        );
    }

    @PostMapping("/generer")
    public ResponseEntity<Map<String, Integer>> generer() {
        int creees = relanceCommercialService.genererRelances(LocalDate.now());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("relancesCreees", creees));
    }

    /** Un commercial ne voit que les relances qui lui sont affectées. */
    private Long identifiantCommercial(
            Long demande, Authentication authentication) {

        if (!estCommercial(authentication)) {
            return demande;
        }
        Long id = commercialService.findIdByUtilisateurEmail(
                authentication.getName());
        return id != null ? id : -1L;
    }

    private boolean accessible(
            RelanceDto relance, Authentication authentication) {
        if (!estCommercial(authentication)) {
            return true;
        }
        Long id = commercialService.findIdByUtilisateurEmail(
                authentication.getName());
        return id != null && id.equals(relance.commercialId());
    }

    private static boolean estCommercial(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_COMMERCIAL"));
    }
}
