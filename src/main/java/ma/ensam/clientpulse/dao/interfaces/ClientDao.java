package ma.ensam.clientpulse.dao.interfaces;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClientDao {

    Client save(Client client);

    Optional<Client> findById(Long id);

    Page<Client> search(
        String keyword,
        StatutClient statut,
        TypeClient type,
        String ville,
        Long commercialId,
        Pageable pageable
    );

    boolean existsByCode(String code);
}
