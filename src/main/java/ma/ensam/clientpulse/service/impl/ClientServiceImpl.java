package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.CommercialDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Commercial;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.TypeClient;
import ma.ensam.clientpulse.dto.ClientDto;
import ma.ensam.clientpulse.dto.ClientRequest;
import ma.ensam.clientpulse.exception.DoublonException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.ClientService;
import ma.ensam.clientpulse.service.mapper.ClientMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;
    private final CommercialDao commercialDao;
    private final ClientMapper clientMapper;

    public ClientServiceImpl(
            ClientDao clientDao,
            CommercialDao commercialDao,
            ClientMapper clientMapper) {
        this.clientDao = clientDao;
        this.commercialDao = commercialDao;
        this.clientMapper = clientMapper;
    }

    @Override
    public ClientDto create(ClientRequest request) {

        if (clientDao.existsByCode(request.code())) {
            throw new DoublonException(
                "Un client avec ce code existe déjà"
            );
        }

        Client client = clientMapper.toEntity(request);

        affecterCommercial(client, request.commercialId());

        return clientMapper.toDto(clientDao.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {

        Client client = clientDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Client introuvable : " + id
                )
            );

        return clientMapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> search(
            String keyword,
            StatutClient statut,
            TypeClient type,
            String ville,
            Long commercialId,
            Pageable pageable) {

        return clientDao.search(
            keyword,
            statut,
            type,
            ville,
            commercialId,
            pageable
        ).map(clientMapper::toDto);
    }

    @Override
    public ClientDto update(Long id, ClientRequest request) {

        Client client = clientDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Client introuvable : " + id
                )
            );

        boolean codeModifie =
            !client.getCode().equals(request.code());

        if (codeModifie && clientDao.existsByCode(request.code())) {
            throw new DoublonException(
                "Un client avec ce code existe déjà"
            );
        }

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

        affecterCommercial(client, request.commercialId());

        return clientMapper.toDto(clientDao.save(client));
    }

    @Override
    public void deactivate(Long id) {

        Client client = clientDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Client introuvable : " + id
                )
            );

        client.setStatut(StatutClient.INACTIF);
        clientDao.save(client);
    }

    private void affecterCommercial(Client client, Long commercialId) {

        if (commercialId == null) {
            client.setCommercial(null);
            return;
        }

        Commercial commercial = commercialDao.findById(commercialId)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Commercial introuvable : " + commercialId
                )
            );

        client.setCommercial(commercial);
    }
}
