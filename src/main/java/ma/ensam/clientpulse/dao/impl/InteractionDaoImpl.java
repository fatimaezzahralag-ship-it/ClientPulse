package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.InteractionDao;
import ma.ensam.clientpulse.dao.jpa.InteractionJpaRepository;
import ma.ensam.clientpulse.dao.jpa.specification.InteractionSpecifications;
import ma.ensam.clientpulse.domain.entity.Interaction;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InteractionDaoImpl implements InteractionDao {

    private final InteractionJpaRepository repository;

    public InteractionDaoImpl(
            InteractionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Interaction save(Interaction interaction) {
        return repository.save(interaction);
    }

    @Override
    public Optional<Interaction> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<Interaction> search(
            Long clientId,
            TypeInteraction type,
            StatutInteraction statut,
            Pageable pageable) {

        Specification<Interaction> specification =
            InteractionSpecifications.belongsToClient(clientId)
                .and(InteractionSpecifications.hasType(type))
                .and(InteractionSpecifications.hasStatut(statut));

        return repository.findAll(specification, pageable);
    }
}
