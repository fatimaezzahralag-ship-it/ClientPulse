package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.ProduitDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProduitService {

    Page<ProduitDto> lister(String motCle, Pageable pageable);

    ProduitDto trouver(Long id);

    ProduitDto creer(ProduitDto dto);

    ProduitDto modifier(Long id, ProduitDto dto);

    ProduitDto desactiver(Long id);
}
