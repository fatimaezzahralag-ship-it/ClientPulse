package ma.ensam.clientpulse.service.impl;

import ma.ensam.clientpulse.dao.interfaces.ClientDao;
import ma.ensam.clientpulse.dao.interfaces.InteractionDao;
import ma.ensam.clientpulse.domain.entity.Client;
import ma.ensam.clientpulse.domain.entity.Interaction;
import ma.ensam.clientpulse.domain.enums.StatutClient;
import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;
import ma.ensam.clientpulse.dto.InteractionDto;
import ma.ensam.clientpulse.dto.InteractionRequest;
import ma.ensam.clientpulse.exception.RegleMetierException;
import ma.ensam.clientpulse.exception.RessourceIntrouvableException;
import ma.ensam.clientpulse.service.interfaces.InteractionService;
import ma.ensam.clientpulse.service.mapper.InteractionMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InteractionServiceImpl implements InteractionService {

    private final InteractionDao interactionDao;
    private final ClientDao clientDao;
    private final InteractionMapper interactionMapper;

    public InteractionServiceImpl(
            InteractionDao interactionDao,
            ClientDao clientDao,
            InteractionMapper interactionMapper) {
        this.interactionDao = interactionDao;
        this.clientDao = clientDao;
        this.interactionMapper = interactionMapper;
    }

    @Override
    public InteractionDto ajouter(
            Long clientId,
            InteractionRequest request) {

        Client client = clientDao.findById(clientId)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Client introuvable : " + clientId
                )
            );

        if (client.getStatut() == StatutClient.INACTIF) {
            throw new RegleMetierException(
                "Impossible de créer une interaction pour un client inactif"
            );
        }

        Interaction interaction =
            interactionMapper.toEntity(request);

        interaction.setClient(client);

        return interactionMapper.toDto(
            interactionDao.save(interaction)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InteractionDto> listerParClient(
            Long clientId,
            TypeInteraction type,
            StatutInteraction statut,
            Pageable pageable) {

        if (clientDao.findById(clientId).isEmpty()) {
            throw new RessourceIntrouvableException(
                "Client introuvable : " + clientId
            );
        }

        return interactionDao
            .search(clientId, type, statut, pageable)
            .map(interactionMapper::toDto);
    }

    @Override
    public InteractionDto changerStatut(Long id, String statut) {

        Interaction interaction = interactionDao.findById(id)
            .orElseThrow(() ->
                new RessourceIntrouvableException(
                    "Interaction introuvable : " + id
                )
            );

        interaction.setStatut(
            StatutInteraction.valueOf(statut)
        );

        return interactionMapper.toDto(
            interactionDao.save(interaction)
        );
    }
}
