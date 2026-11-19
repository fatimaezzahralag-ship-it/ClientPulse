package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.DevisDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DevisService {

    Page<DevisDto> lister(Pageable pageable);

    DevisDto trouver(Long id);

    DevisDto creer(DevisDto dto);

    DevisDto modifier(Long id, DevisDto dto);

    void supprimer(Long id);

    DevisDto envoyer(Long id);

    DevisDto accepter(Long id);

    DevisDto refuser(Long id);
}
