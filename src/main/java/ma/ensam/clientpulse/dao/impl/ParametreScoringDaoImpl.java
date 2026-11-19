package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.ParametreScoringDao;
import ma.ensam.clientpulse.dao.jpa.ParametreScoringJpaRepository;
import ma.ensam.clientpulse.domain.entity.ParametreScoring;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ParametreScoringDaoImpl implements ParametreScoringDao {

    private final ParametreScoringJpaRepository repository;

    public ParametreScoringDaoImpl(
            ParametreScoringJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ParametreScoring> findByNom(String nom) {
        return repository.findByNom(nom);
    }

    @Override
    public Optional<ParametreScoring> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ParametreScoring> findAll() {
        return repository.findAllByOrderByNomAsc();
    }

    @Override
    public ParametreScoring save(ParametreScoring parametre) {
        return repository.save(parametre);
    }
}
