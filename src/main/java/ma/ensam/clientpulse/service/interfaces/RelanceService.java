package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.NatureRelance;
import ma.ensam.clientpulse.domain.enums.StatutRelance;
import ma.ensam.clientpulse.dto.RelanceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelanceService {

    Page<RelanceDto> lister(NatureRelance nature, Pageable pageable);

    RelanceDto trouver(Long id);

    RelanceDto creer(RelanceDto dto);

    RelanceDto changerStatut(Long id, StatutRelance statut, String resultat);

    boolean existePourFacture(Long factureId, NatureRelance nature, int niveau);
}
