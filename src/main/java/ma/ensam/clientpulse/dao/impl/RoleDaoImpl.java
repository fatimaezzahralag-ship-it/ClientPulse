package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.RoleDao;
import ma.ensam.clientpulse.dao.jpa.RoleJpaRepository;
import ma.ensam.clientpulse.domain.entity.Role;
import ma.ensam.clientpulse.domain.enums.RoleNom;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleJpaRepository repository;

    public RoleDaoImpl(RoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role save(Role role) {
        return repository.save(role);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Role> findByNom(RoleNom nom) {
        return repository.findByNom(nom);
    }

    @Override
    public boolean existsByNom(RoleNom nom) {
        return repository.existsByNom(nom);
    }
}
