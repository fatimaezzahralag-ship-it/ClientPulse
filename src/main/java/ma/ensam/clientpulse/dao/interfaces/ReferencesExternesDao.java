package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;

import java.util.Optional;

/**
 * Lecture seule des entités qui appartiennent à P2 (Client, Commercial).
 * Les devis, factures et relances ont besoin de les retrouver par leur id.
 * Si P2 expose plus tard un ClientDao, on pourra remplacer cette interface par la sienne.
 */
public interface ReferencesExternesDao {

    Optional<Client> trouverClient(Long id);

    Optional<Commercial> trouverCommercial(Long id);
}
