package ma.ensam.clientpulse.service.mapper;

import lombok.RequiredArgsConstructor;
import ma.ensam.clientpulse.domain.entity.Facture;
import ma.ensam.clientpulse.domain.entity.LigneFacture;
import ma.ensam.clientpulse.dto.FactureDto;
import ma.ensam.clientpulse.dto.LigneDto;
import ma.ensam.clientpulse.dto.PaiementDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FactureMapper {

    private final PaiementMapper paiementMapper;

    public FactureDto toDto(Facture f) {
        List<LigneDto> lignes = f.getLignes().stream().map(this::ligneToDto).toList();
        List<PaiementDto> paiements = f.getPaiements().stream().map(paiementMapper::toDto).toList();
        Long devisId = f.getDevis() == null ? null : f.getDevis().getId();
        Long origineId = f.getFactureOrigine() == null ? null : f.getFactureOrigine().getId();
        // totalPaye et resteAPayer ne sont pas en base : l'entité les calcule à la demande
        return new FactureDto(f.getId(), f.getNumero(), f.getType(), f.getDateEmission(), f.getDateEcheance(),
                f.getStatut(), f.getClient().getId(), devisId, origineId, lignes, paiements,
                f.getTotalHT(), f.getTotalTVA(), f.getTotalTTC(), f.getTotalPaye(), f.getResteAPayer());
    }

    private LigneDto ligneToDto(LigneFacture l) {
        Long produitId = l.getProduit() == null ? null : l.getProduit().getId();
        return new LigneDto(l.getId(), produitId, l.getDesignation(), l.getQuantite(), l.getPrixUnitaireHT(),
                l.getRemise(), l.getTauxTVA(), l.getMontantHT(), l.getMontantTVA());
    }
}
