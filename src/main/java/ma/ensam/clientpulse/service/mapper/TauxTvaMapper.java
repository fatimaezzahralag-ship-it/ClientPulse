package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.TauxTVA;
import ma.ensam.clientpulse.dto.TauxTvaDto;
import org.springframework.stereotype.Component;

/** Un mapper traduit une entité en DTO (et inversement). Écrit à la main : on voit tout ce qui se passe. */
@Component
public class TauxTvaMapper {

    public TauxTvaDto toDto(TauxTVA t) {
        return new TauxTvaDto(t.getId(), t.getLibelle(), t.getTaux(), t.isActif());
    }

    /** Recopie les champs modifiables du DTO dans l'entité (id et actif ne sont pas touchés). */
    public void copier(TauxTvaDto dto, TauxTVA cible) {
        cible.setLibelle(dto.libelle());
        cible.setTaux(dto.taux());
    }
}
