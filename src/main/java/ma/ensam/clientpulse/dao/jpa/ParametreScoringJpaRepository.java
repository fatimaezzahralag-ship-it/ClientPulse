package ma.ensam.clientpulse.dao.jpa;

import ma.ensam.clientpulse.domain.entity.ParametreScoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParametreScoringJpaRepository
        extends JpaRepository<ParametreScoring, Long> {

    Optional<ParametreScoring> findByNom(String nom);

    List<ParametreScoring> findAllByOrderByNomAsc();
}
