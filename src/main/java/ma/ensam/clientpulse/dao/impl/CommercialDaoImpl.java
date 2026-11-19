package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.dao.jpa.CommercialJpaRepository;
import ma.ensam.clientpulse.domain.entity.Commercial;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommercialDaoImpl implements CommercialDao {

    private final CommercialJpaRepository repository;

    public CommercialDaoImpl(CommercialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Commercial save(Commercial commercial) {
        return repository.save(commercial);
    }

    @Override
    public List<Commercial> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Commercial> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Commercial> findByUtilisateurId(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId);
    }

    @Override
    public boolean existsByUtilisateurId(Long utilisateurId) {
        return repository.existsByUtilisateurId(utilisateurId);
    }
}
