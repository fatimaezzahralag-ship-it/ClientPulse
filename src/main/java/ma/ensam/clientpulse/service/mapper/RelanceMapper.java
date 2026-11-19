package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Relance;
import ma.ensam.clientpulse.dto.RelanceDto;
import org.springframework.stereotype.Component;

@Component
public class RelanceMapper {

    public RelanceDto toDto(Relance r) {
        Long factureId = r.getFacture() == null ? null : r.getFacture().getId();
        return new RelanceDto(r.getId(), r.getNature(), r.getNiveau(), r.getDateCreation(), r.getEcheance(),
                r.getStatut(), r.getResultat(), r.getContenu(), r.isSuggereeParSysteme(),
                r.getClient().getId(), factureId, r.getAffecteAId());
    }
}
