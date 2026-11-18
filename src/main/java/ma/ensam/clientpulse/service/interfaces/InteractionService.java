package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.domain.enums.StatutInteraction;
import ma.ensam.clientpulse.domain.enums.TypeInteraction;
import ma.ensam.clientpulse.dto.InteractionDto;
import ma.ensam.clientpulse.dto.InteractionRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InteractionService {

    InteractionDto ajouter(Long clientId, InteractionRequest request);

    Page<InteractionDto> listerParClient(
        Long clientId,
        TypeInteraction type,
        StatutInteraction statut,
        Pageable pageable
    );

    InteractionDto changerStatut(Long id, String statut);
}
