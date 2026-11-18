package ma.ensam.clientpulse.dao.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.jpa.ClientJpaRepository;
import ma.ensam.clientpulse.dao.jpa.specification.ClientSpecifications;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ClientDaoImpl implements ClientDao {

    private final ClientJpaRepository repository;

    public ClientDaoImpl(ClientJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Client save(Client client) {
        return repository.save(client);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Page<Client> search(
            String keyword,
            StatutClient statut,
            TypeClient type,
            String ville,
            Long commercialId,
            Pageable pageable) {

        Specification<Client> specification =
            ClientSpecifications.search(keyword)
                .and(ClientSpecifications.hasStatut(statut))
                .and(ClientSpecifications.hasType(type))
                .and(ClientSpecifications.hasVille(ville))
                .and(ClientSpecifications.hasCommercial(commercialId));

        return repository.findAll(specification, pageable);
    }
}
