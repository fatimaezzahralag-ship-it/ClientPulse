package ma.ensam.clientpulse.service.mapper;

import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.dto.ClientDto;
import ma.ensam.clientpulse.dto.ClientRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientRequest request) {

        Client client = new Client();

        client.setCode(request.code());
        client.setRaisonSociale(request.raisonSociale());
        client.setIce(request.ice());
        client.setType(request.type());
        client.setEmail(request.email());
        client.setTelephone(request.telephone());
        client.setAdresse(request.adresse());
        client.setVille(request.ville());
        client.setSecteur(request.secteur());
        client.setDelaiPaiementJours(
            request.delaiPaiementJours()
        );
        client.setStatut(request.statut());

        return client;
    }

    public ClientDto toDto(Client client) {

        return new ClientDto(
            client.getId(),
            client.getCode(),
            client.getRaisonSociale(),
            client.getIce(),
            client.getType(),
            client.getEmail(),
            client.getTelephone(),
            client.getAdresse(),
            client.getVille(),
            client.getSecteur(),
            client.getDelaiPaiementJours(),
            client.getStatut(),
            client.getCommercial() != null
                ? client.getCommercial().getId()
                : null
        );
    }
}
