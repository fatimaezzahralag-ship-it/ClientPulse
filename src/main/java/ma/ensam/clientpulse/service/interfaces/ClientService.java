package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.dto.ClientDto;
import ma.ensam.clientpulse.dto.ClientRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ClientDto create(ClientRequest request);

    ClientDto findById(Long id);

    Page<ClientDto> search(
        String keyword,
        StatutClient statut,
        TypeClient type,
        String ville,
        Long commercialId,
        Pageable pageable
    );

    ClientDto update(Long id, ClientRequest request);

    void deactivate(Long id);
}
