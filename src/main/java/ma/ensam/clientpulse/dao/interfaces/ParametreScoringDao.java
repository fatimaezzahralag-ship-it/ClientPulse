package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.ParametreScoring;

import java.util.List;
import java.util.Optional;

public interface ParametreScoringDao {

    Optional<ParametreScoring> findByNom(String nom);

    Optional<ParametreScoring> findById(Long id);

    List<ParametreScoring> findAll();

    ParametreScoring save(ParametreScoring parametre);
}
