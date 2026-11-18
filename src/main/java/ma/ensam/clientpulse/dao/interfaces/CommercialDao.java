package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Commercial;

import java.util.Optional;

public interface CommercialDao {

    Commercial save(Commercial commercial);

    Optional<Commercial> findById(Long id);

    Optional<Commercial> findByUtilisateurId(Long utilisateurId);

    boolean existsByUtilisateurId(Long utilisateurId);
}
