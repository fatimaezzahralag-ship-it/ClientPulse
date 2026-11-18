package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.Commercial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommercialJpaRepository extends JpaRepository<Commercial, Long> {

    Optional<Commercial> findByUtilisateurId(Long utilisateurId);

    boolean existsByUtilisateurId(Long utilisateurId);
}
