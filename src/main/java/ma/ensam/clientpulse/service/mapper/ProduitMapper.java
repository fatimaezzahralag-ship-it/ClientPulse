package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Produit;
import ma.ensam.clientpulse.domain.entity.TauxTVA;
import ma.ensam.clientpulse.dto.ProduitDto;
import org.springframework.stereotype.Component;

@Component
public class ProduitMapper {

    public ProduitDto toDto(Produit p) {
        TauxTVA taux = p.getTauxTVA();
        return new ProduitDto(p.getId(), p.getReference(), p.getDesignation(), p.getPrixUnitaireHT(),
                p.getMarge(), p.isActif(), taux.getId(), taux.getTaux());
    }

    /** Recopie les champs modifiables du DTO dans l'entité. Le taux est déjà chargé par le service. */
    public void copier(ProduitDto dto, Produit cible, TauxTVA taux) {
        cible.setReference(dto.reference());
        cible.setDesignation(dto.designation());
        cible.setPrixUnitaireHT(dto.prixUnitaireHT());
        cible.setMarge(dto.marge());
        cible.setTauxTVA(taux);
    }
}
