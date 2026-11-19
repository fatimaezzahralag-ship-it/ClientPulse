package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.DevisDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DevisService {

    Page<DevisDto> lister(Pageable pageable);

    DevisDto trouver(Long id);

    /** Crée un devis BROUILLON : numéro DEV-AAAA-NNNN et totaux calculés par le serveur. */
    DevisDto creer(DevisDto dto);

    /** Modifie un brouillon. Un devis envoyé n'est plus modifiable. */
    DevisDto modifier(Long id, DevisDto dto);

    /** Supprime un brouillon. */
    void supprimer(Long id);

    DevisDto envoyer(Long id);

    DevisDto accepter(Long id);

    DevisDto refuser(Long id);
}
