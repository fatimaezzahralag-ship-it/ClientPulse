package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.UtilisateurDao;
import ma.ensam.clientpulse.dao.jpa.UtilisateurJpaRepository;
import ma.ensam.clientpulse.domain.entity.Utilisateur;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UtilisateurDaoImpl implements UtilisateurDao {

    private final UtilisateurJpaRepository repository;

    public UtilisateurDaoImpl(
            UtilisateurJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Utilisateur> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Utilisateur> findAll() {
        return repository.findAll();
    }

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        return repository.save(utilisateur);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
